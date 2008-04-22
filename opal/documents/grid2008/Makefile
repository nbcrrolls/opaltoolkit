# -*- Mode: makefile -*-
#
# Simple makefile for creating pdf from latex source

SHELL = /bin/sh

FILE = paper

all: pdf

pdf: $(FILE).tex
	pdflatex $(FILE).tex  && bibtex $(FILE);
	pdflatex $(FILE).tex && pdflatex $(FILE).tex

clean:
	/bin/rm -f $(FILE).aux $(FILE).bbl $(FILE).blg $(FILE).log $(FILE).pdf
