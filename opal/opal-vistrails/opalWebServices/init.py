import core.modules.module_registry
from core.modules.vistrails_module import Module, ModuleError
from core.modules.vistrails_module import new_module, Module

import sys
import time
import httplib
import urllib
import xml.dom.minidom
import os
import shutil
import subprocess
import tempfile
import urlparse

from AppService_client import AppServiceLocator, launchJobRequest, \
    getOutputsRequest, queryStatusRequest, destroyRequest, getAppMetadataRequest
from AppService_types import ns0

version = "0.9.0"
name = "OpalWebServices"
identifier = "edu.utah.sci.vistrails.OpalWebServices"

def expand_ports(port_list):
    new_port_list = []
    for port in port_list:
        port_spec = port[1]
        if type(port_spec) == str: # or unicode...
            if port_spec.startswith('('):
                port_spec = port_spec[1:]
            if port_spec.endswith(')'):
                port_spec = port_spec[:-1]
            new_spec_list = []
            for spec in port_spec.split(','):
                spec = spec.strip()
                parts = spec.split(':', 1)
#                print 'parts:', parts
                namespace = None
                if len(parts) > 1:
                    mod_parts = parts[1].rsplit('|', 1)
                    if len(mod_parts) > 1:
                        namespace, module_name = mod_parts
                    else:
                        module_name = parts[1]
                    if len(parts[0].split('.')) == 1:
                        id_str = 'edu.utah.sci.vistrails.' + parts[0]
                    else:
                        id_str = parts[0]
                else:
                    mod_parts = spec.rsplit('|', 1)
                    if len(mod_parts) > 1:
                        namespace, module_name = mod_parts
                    else:
                        module_name = spec
                    id_str = identifier
                if namespace:
                    new_spec_list.append(id_str + ':' + module_name + ':' + \
                                             namespace)
                else:
                    new_spec_list.append(id_str + ':' + module_name)
            port_spec = '(' + ','.join(new_spec_list) + ')'
        new_port_list.append((port[0], port_spec) + port[2:])
#    print new_port_list
    return new_port_list

def build_modules(module_descs):
    new_classes = {}
    for m_name, m_attrs, m_dict in module_descs:
        m_doc = m_attrs.get("_doc", None)
        m_inputs = m_attrs.get("_inputs", [])
        m_outputs = m_attrs.get("_outputs", [])
        if "_inputs" in m_attrs:
            del m_attrs["_inputs"]
        if "_outputs" in m_attrs:
            del m_attrs["_outputs"]
        if "_doc" in m_attrs:
            del m_attrs["_doc"]
            
        m_class = new_module(Module, m_name, m_dict, m_doc)
        m_class._input_ports = expand_ports(m_inputs)
        m_class._output_ports = expand_ports(m_outputs)
        new_classes[m_name] = (m_class, m_attrs)
    return new_classes.values()

def get_ports(ports):
    clean_ports = []
    for p in ports:
        if len(p) > 3 and type(p[2]) == type({}):
            clean_ports.append(p[:3])
        else:
            clean_ports.append(p)
    return clean_ports

def getUntaggedParam(meta,tagid):
    """
    getUntaggedParam description:
    @input:
    -meta: application's metadata
    -tagid: id for parameter
    @output:
    -returns param object
    """
    untaggedlist = meta._types._untaggedParams._param
    for i in untaggedlist:
        if(i._id==tagid):
            return i
    return "NOT AN UNTAGGED PARAM"


def getTaggedParam(meta,tagid):
    """
    getTaggedParam description:
    @input:
    -meta: application's metadata
    -tagid: id for parameter
    @output:
    -returns param object
    """
    taggedlist = meta._types._taggedParams._param
    for i in taggedlist:
        if(i._id==tagid):
            return i
    return "NOT AN UNTAGGED PARAM"
 
def initialize():
    global _modules

    if configuration.check('urlList'):
        urlList = configuration.urlList.split(";")
    else:
        return

    module_descs = []

    opal_url = urlList[0]

    if not opal_url:
        print "ERROR: No Opal URL provided"
        return
    
    services = []

    for opal_url in urlList:
        if opal_url.find("/opal2") != -1:
            service_list_url = opal_url + "/opalServices.xml"
            opener = urllib.FancyURLopener({})
            socket = opener.open(service_list_url)
            text = socket.read()
            feed = xml.dom.minidom.parseString(text)
            for entry in feed.getElementsByTagName('entry'):
                link = entry.getElementsByTagName('link')[0]
                service = link.getAttribute('href')
                services.append(str(service))
        else:
            print "ERROR: No opal2 contained in URL"
            return

    for url in services:
        namespace = os.path.dirname(os.path.dirname(url))
        servername = urlparse.urlparse(url)[1]
        servername = servername.replace('.','_')
        servername = servername.split(':')[0]
        node_name = os.path.basename(url) + '_' + servername
        appLocator = AppServiceLocator()
        appServicePort = appLocator.getAppServicePort(url)
        req = getAppMetadataRequest()
        metadata = appServicePort.getAppMetadata(req)

        if url == 'http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService':
            print "------------------------\n"
            print metadata
            print "------------------------\n"

        ip_list = []
        pdesc_dic = {}

        try:
            separator = metadata._types._taggedParams._separator

            if separator == None:
                separator = " "
        except AttributeError:
            separator = " "
            
        try:
            rawlist = metadata._types._flags._flag

            for i in rawlist:
                pdesc_dic[str(i._id)] = str(i._textDesc)
                meta = {'url': url, 'type': 'FLAG', 'file': False, 'arg': str(i._tag), 'default': None, 'adv': True}
                if i._default and str(i._default) == 'True':
                    meta['default'] = 'True'
                    ip_list.append((str(i._id), "basic:Boolean", {"defaults": str([str('True')]), "labels": str([str(i._textDesc)]), "optional": True}, meta))
                else:
                    ip_list.append((str(i._id), "basic:Boolean", {"labels": str([str(i._textDesc)]), "optional": True}, meta))
        except AttributeError:
            flaglist = []
            
        try:
            rawlist = metadata._types._taggedParams._param

            for i in rawlist:
                #print "TAGID is " + str(i._tag)
                meta = {'url': url, 'type': 'TAGGED', 'file': False, 'default': None, 'adv': True}
                meta['arg'] = str(i._tag) + str(separator)
                pdesc_dic[str(i._id)] = str(i._textDesc)
                tagged_object = getTaggedParam(metadata, str(i._id))

                if (tagged_object._paramType == 'FILE') and (tagged_object._ioType == 'INPUT'):
                    meta['file'] = True
                    
                    if i._default:
                        meta['default'] = str(i._default)
                        ip_list.append((str(i._id), "basic:File", {"defaults": str([str(i._default)]), "labels": str([str(i._textDesc)]), "optional": True}, meta))
                    else:
                        ip_list.append((str(i._id), "basic:File", {"labels": str([str(i._textDesc)]), "optional": True}, meta))
                else:
                    meta['file'] = False

                    if i._default:
                        meta['default'] = str(i._default)
                        ip_list.append((str(i._id), "basic:String", {"defaults": str([str(i._default)]), "labels": str([str(i._textDesc)]), "optional": True}, meta))
                    else:
                        ip_list.append((str(i._id), "basic:String", {"labels": str([str(i._textDesc)]), "optional": True}, meta))
        except AttributeError:
            taglist = []

        try:
            rawlist = metadata._types._untaggedParams._param
            #untaglist = [str(i._id) for i in rawlist]
            for i in rawlist:
                meta = {'url': url, 'type': 'UNTAGGED', 'file': False, 'default': None, 'adv': True}
                meta['arg'] = str(i._id) 
                pdesc_dic[str(i._id)] = str(i._textDesc)                
                untagged_object = getUntaggedParam(metadata, meta['arg'])

                if (untagged_object._paramType=='FILE') and (untagged_object._ioType=='INPUT'):
                    meta['file'] = True
                    ip_list.append((str(i._id), "basic:File", {"labels": str([str(i._textDesc)]), "optional": True}, meta))
                else:
                    if i._default:
                        meta['default'] = str(i._default)
                        ip_list.append((str(i._id), "basic:String", {"defaults": str([str(i._default)]), "labels": str([str(i._textDesc)]), "optional": True}, meta))
                    else:
                        ip_list.append((str(i._id), "basic:String", {"labels": str([str(i._textDesc)]), "optional": True}, meta))
        except AttributeError:
            untaglist = []

        pdesc_keys = pdesc_dic.keys()
        pdesc_keys.sort()

        mod_doc = metadata._usage + "\n"
        mod_doc += "\n***************** Input Parameters ****************\n\n"

        for pk in pdesc_keys:
            mod_doc += pk + ": " + pdesc_dic[pk] + "\n"

        if ip_list == []:
            meta = {'url': url, 'type': 'simple', 'default': None, 'adv': False}
            ip_list.append(("commandLine", "basic:String", {"labels": str(["Command line arguments"]), "optional": True}, meta))
            ip_list.append(("inFiles", "basic:List", {"labels": str(["Input file list"]), "optional": True}, meta))
            ip_list.append(("numProcs", "basic:Integer", {"labels": str(["Number of processors"]), "optional": True}, meta))

        def ws_compute(self):
            if self._port_list[0][3]['url'] != None:
                url = self._port_list[0][3]['url']
                adv = self._port_list[0][3]['adv']
            else:
                url = ""
                print "ERROR: There are no input arguments provided for this web service"

            cmdline = ""
            numProcs = None
            tagged = []
            untagged = []
            flags = []
            files = []

            for i in self._port_list:
                pn = i[0]
                meta = i[3]

                if ((self.hasInputFromPort(pn) and self.getInputFromPort(pn) != "") or meta['default'] != None) and adv == True:
#                    print "META"
#                    print meta

                    if meta['type'] == 'FLAG':
                        if self.hasInputFromPort(pn):
                            if self.getInputFromPort(pn) == True:
                                flags.append(meta['arg'])
                        elif meta['default'] == 'True':
                            flags.append(meta['arg'])
                    elif meta['type'] == 'TAGGED':
                        if self.hasInputFromPort(pn):
                            val = self.getInputFromPort(pn)
                        elif meta['default'] != None:
                            val = meta['default']

                        if meta['file'] == True:
                            file_an = core.modules.basic_modules.File.translate_to_string(val)
                            file_bn = os.path.basename(file_an)
                            tagged.append(meta['arg'] + file_bn)
                            files.append(file_an)
                        else:
                            tagged.append(meta['arg'] + val)
                    elif meta['type'] == 'UNTAGGED':
                        if self.hasInputFromPort(pn):
                            val = self.getInputFromPort(pn)
                        elif meta['default'] != None:
                            val = meta['default']

                        if meta['file'] == True:
                            file_an = core.modules.basic_modules.File.translate_to_string(val)
                            file_bn = os.path.basename(file_an)
                            untagged.append(file_bn)
                            files.append(file_an)
                        else:
                            untagged.append(val)
                            
                    cmdline = ""

                    for i in flags:
                        cmdline += i + " "
                    for i in tagged:
                        cmdline += i + " "
                    for i in untagged:
                        cmdline += i + " "

            inputFiles = []

            for i in files:
                inputFile = ns0.InputFileType_Def('inputFile')
                inputFile._name = os.path.basename(i)                     
                inputFile._attachment = open(i, "r")
                inputFiles.append(inputFile)

            if cmdline == "":
                if self.hasInputFromPort('commandLine'):
                    cmdline = self.getInputFromPort('commandLine')
                if self.hasInputFromPort('numProcs'):
                    numProcs = self.getInputFromPort('numProcs')
                if self.hasInputFromPort("inFiles"):
                    inFilesPath = self.getInputFromPort("inFiles")
                    if inFilesPath != None:
                        for i in inFilesPath:
                            inputFile = ns0.InputFileType_Def('inputFile')
                            inputFile._name = os.path.basename(i)                     
                            inputFile._attachment = open(i, "r")
                            inputFiles.append(inputFile)
            
            print os.path.basename(url) + " from " + os.path.dirname(url) + " is going to run with arguments:\n     " + cmdline 

            appLocator = AppServiceLocator()
            appServicePort = appLocator.getAppServicePort(url)
            req = launchJobRequest()
            req._argList = cmdline
            req._inputFile = inputFiles
            if numProcs != None:
                req._numProcs = numProcs
            resp = appServicePort.launchJob(req)
            self.jobID = resp._jobID
            print "Job outputs URL: " + resp._status._baseURL # urlparse.urljoin(url, '/' + self.jobID)
            status = resp._status._code

            while (status != 4 and status != 8):
                status = appServicePort.queryStatus(queryStatusRequest(self.jobID))._code
                time.sleep(5)
             
            if (status == 8):
                resp = appServicePort.getOutputs(getOutputsRequest(self.jobID))
                outurls = [str(resp._stdOut), str(resp._stdErr)]
                if (resp._outputFile != None):
                    for i in resp._outputFile:
                        outurls.append(str(i._url))
                    print "Opal job completed successfully"
            else:
                print "ERROR: Opal job failed"
                resp = appServicePort.getOutputs(getOutputsRequest(self.jobID))
                outurls = [str(resp._stdOut), str(resp._stdErr)]

            self.setResult("outurls", tuple(outurls))

        module_descs.append((node_name, 
                             {"_inputs": get_ports(ip_list),                        
                              "_outputs": [("outurls", "basic:List")],
                              "_doc": mod_doc,
                              "namespace": namespace},
                             {"compute": ws_compute,            
                              "_port_list": ip_list,
                              }))

              
#    if configuration.use_b:
    _modules = build_modules(module_descs)
#    else:
#        _modules = build_modules(module_descs[:1])

_modules = []

#class ExecuteOpalJob(Module):
#    """This module runs opal web services."""

#    def compute(self):
#         url = self.getInputFromPort("url")
         
#         if self.hasInputFromPort("commandline"):
#             commandline = self.getInputFromPort("commandline")
#         else:
#             print "WARNING: commandline port missing"
#             commandline = None
         
#         print url + " is going to execute the command: " + commandline

#         if self.hasInputFromPort("numProcs"):
#             numProcs = self.getInputFromPort("numProcs")
#         else:
#             numProcs = None

#         inputFiles = []

#         if self.hasInputFromPort("inFilesPath"):
#             inFilesPath = self.getInputFromPort("inFilesPath")
#             if inFilesPath != None:
#                 for i in inFilesPath:
#                     inputFile = ns0.InputFileType_Def('inputFile')
#                     inputFile._name = os.path.basename(i)                     
#                     inputFile._attachment = open(i, "r")
#                     inputFiles.append(inputFile)

#         appLocator = AppServiceLocator()
#         appServicePort = appLocator.getAppServicePort(url)
#         req = launchJobRequest()
#         req._argList = commandline
#         req._inputFile = inputFiles
#         req._numProcs = numProcs
#         resp = appServicePort.launchJob(req)
#         self.jobID = resp._jobID
#         print "Job outputs URL: " + resp._status._baseURL # urlparse.urljoin(url, '/' + self.jobID)
#         status = resp._status._code
         
#         while (status != 4 and status != 8):
#             status = appServicePort.queryStatus(queryStatusRequest(self.jobID))._code
#             time.sleep(5)
             
#         if (status == 8):
#             resp = appServicePort.getOutputs(getOutputsRequest(self.jobID))
#             outurls = [str(resp._stdOut), str(resp._stdErr)]
#             if (resp._outputFile != None):
#                for i in resp._outputFile:
#                     outurls.append(str(i._url))
#             print "Opal job completed successfully"
#         else:
#             print "WARNING: Opal job failed"
#             resp = appServicePort.getOutputs(getOutputsRequest(self.jobID))
#             outurls = [str(resp._stdOut),str(resp._stdErr)]

#         self.setResult("outurls", tuple(outurls))
        
#######################################

#def initialize(*args, **keywords):
#    import core.packagemanager
#    global schema
#    global webServicesmodulesDict
#    global complexsdict
#    urlList = []
#    if configuration.check('urlList'):
#        urlList = configuration.urlList.split(";")

#    for i in urlList:
#        print "URL = " + i

#    reg = core.modules.module_registry.registry

#    reg.add_module(ExecuteOpalJob)
#    reg.add_module(TestOpal)

#    print "DOH PIGGY!!!!!!!!!!!!!!!\n"
#    url = "http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService"

#    appLocator = AppServiceLocator()
#    appServicePort = appLocator.getAppServicePort(url)
#    req = getAppMetadataRequest()
#    metadata = appServicePort.getAppMetadata(req)

#    try:
#        rawlist = metadata._types._flags._flag
#        flaglist = [str(i._id) for i in rawlist]
#    except AttributeError:
#        flaglist = []

#    try:
#        rawlist = metadata._types._taggedParams._param
#        taglist = [str(i._id) for i in rawlist]
#    except AttributeError:
#        taglist = []

#    try:
#        rawlist = metadata._types._untaggedParams._param
#        untaglist = [str(i._id) for i in rawlist]
#    except AttributeError:
#        untaglist = []

#    for i in flaglist:
#        print "FLAGS = " + i

#    for i in taglist:
#        print "TAG = " + i

#    for i in untaglist:
#        print "UNTAG = " + i

#    for i in flaglist:
#        reg.add_input_port(TestOpal, i, (core.modules.basic_modules.Boolean, i))

#    for i in taglist:
#        reg.add_input_port(TestOpal, i, (core.modules.basic_modules.String, i))

#    for i in untaglist:
#        reg.add_input_port(TestOpal, i, (core.modules.basic_modules.String, i))


#    reg.add_input_port(ExecuteOpalJob, "url",
#                       (core.modules.basic_modules.String, 'URL'))
#    reg.add_input_port(ExecuteOpalJob, "commandline",
#                       (core.modules.basic_modules.String, 'commandline'))
#    reg.add_input_port(ExecuteOpalJob, "inFilesPath",
#                       (core.modules.basic_modules.List, 'inFilesPath'))
#    reg.add_input_port(ExecuteOpalJob, "numProcs",
#                       (core.modules.basic_modules.Integer, 'numProcs'))
#    reg.add_output_port(ExecuteOpalJob, "outurls",
#                       (core.modules.basic_modules.List, 'outurls'))
