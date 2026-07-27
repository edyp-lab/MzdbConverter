# msDataConverter

## Getting started

Command line tool to convert Thermo raw File or Brucker .d directories to mzdb files

To display the list of option type : 
* mzdbConverter thermo --help
* mzdbConverter brucker --help

### Linux 

On linux system it may be necessary to 
* enable excution of scripts. 
  * `chmod +x ./mzdbConverter.sh`
  * `chmod +x ./jdk/bin/java`  
  * `chmod +x ./ThermoAccess-<version>/ThermoAccess`

## library

This tool can also be used as a library, especially to read metadata from raw files

## Release History

### 1.4.0

Create distribution for Linux and Windows.

### 1.3.0

### msDataConsumer 
Add a Read Acquisition MetaData method

### ThermoAccess
Version 1.0.3.0 :
* Add command to read acquisition file metadata only, from a file or a folder
* Improve error management 
* Fix Spectrum DIA isolationWindows error