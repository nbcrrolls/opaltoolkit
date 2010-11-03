import core.modules.module_registry
from core.modules.vistrails_module import Module, ModuleError
from core.modules.vistrails_module import new_module, Module

import sys
import time
import httplib
import urllib
import os
import shutil
import subprocess
import tempfile
import urlparse

version = "0.9.0"
name = "OpalWebServiceHelpers"
identifier = "edu.utah.sci.vistrails.OpalWebServiceHelpers"

class GetMainURLFromList(Module):
    def compute(self):
        url_list = self.getInputFromPort('url_list')
        url = os.path.dirname(url_list[0])
            
        self.setResult('url', url)

class GetFileWithExtension(Module):
    def compute(self):
        url_list = self.getInputFromPort('url_list')
        ext = self.getInputFromPort('extension')
        url = 'Not_found'

        if not(ext.startswith('.')):
            ext = '.' + ext

        for i in url_list:
            if i.endswith(ext):
                url = i
                break
            
        self.setResult('url', url)

class Download(Module):
    def compute(self):
        url = self.getInputFromPort('url')
        output_file = self.getInputFromPort('output_file')
            
        webfile = urllib.urlopen(url)
        localfile = open(output_file, 'w')

        print "Downloading " + url + " to local machine as " + output_file
        
        localfile.write(webfile.read())
        webfile.close()
        localfile.close()
        

def initialize(*args, **keywords):
  reg = core.modules.module_registry.registry

  reg.add_module(GetMainURLFromList)
  reg.add_input_port(GetMainURLFromList, 'url_list',
                     (core.modules.basic_modules.List, 'url_list'))
  reg.add_output_port(GetMainURLFromList, 'url',
                      (core.modules.basic_modules.String, 'url'))

  reg.add_module(GetFileWithExtension)
  reg.add_input_port(GetFileWithExtension, 'url_list',
                     (core.modules.basic_modules.List, 'url_list'))
  reg.add_input_port(GetFileWithExtension, 'extension',
                     (core.modules.basic_modules.String, 'extension'))
  reg.add_output_port(GetFileWithExtension, 'url',
                      (core.modules.basic_modules.String, 'url'))

  reg.add_module(Download)
  reg.add_input_port(Download, 'url',
                      (core.modules.basic_modules.String, 'url'))
  reg.add_input_port(Download, 'output_file',
                      (core.modules.basic_modules.String, 'output_file'))


                     
