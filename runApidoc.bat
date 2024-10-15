REM this is a windows batch file to generate the API Documentation of prioritize on a windows machine.
REM NOTE: nvm, nodejs and the apidoc package must be installed for this to work:

REM With nodejs configured use the following commandline to get apidoc:
REM npm install apidoc -g

REM If you run a linux or mac machine please feel free to install nodejs there and perform the steps necessary for your system.
REM If you'd like write a corresponding .sh-File and make a merge request at the prioritize GitHub pages.


apidoc -i ./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary  -o ./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/apidoc/