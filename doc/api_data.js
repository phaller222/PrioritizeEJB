define({ "api": [
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./build/classes/de/hallerweb/enterprise/prioritize/view/boundary/apidoc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_build_classes_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_build_classes_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
    "name": ""
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./build/classes/de/hallerweb/enterprise/prioritize/view/boundary/doc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_build_classes_de_hallerweb_enterprise_prioritize_view_boundary_doc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_build_classes_de_hallerweb_enterprise_prioritize_view_boundary_doc_main_js",
    "name": ""
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./doc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_doc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_doc_main_js",
    "name": ""
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/apidoc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
    "name": ""
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/doc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_doc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_doc_main_js",
    "name": ""
  },
  {
    "type": "get",
    "url": "/calendar/reservations",
    "title": "getTimeSpansForReservations",
    "name": "getTimeSpansForReservations",
    "group": "_calendar",
    "description": "<p>Searches for all resource reservations to resources (devices) within a department. The department is given by the departmentToken parameter. Parameters &quot;from&quot; and &quot;to&quot; indicate the the timespan to search.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>Department token to use.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "from",
            "description": "<p>Java timestamp to indicate the start date from which to search for resevations.</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "to",
            "description": "<p>Java timestamp to indicate the end date to search for resevations.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "TimeSpan",
            "optional": false,
            "field": "timespan",
            "description": "<p>JSON Objects with all timespans currently registered for reservations.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n\t[\n   {\n     \"id\" : 76,\n     \"title\" : \"aaaa\",\n     \"description\" : \"default:aaaa[admin]\",\n     \"dateFrom\" : 1479164400000,\n     \"dateUntil\" : 1485817200000,\n     \"type\" : \"RESOURCE_RESERVATION\",\n     \"department\" : ...list of departments...\n   }\n ]",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>DepartmentToken or APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/CalendarService.java",
    "groupTitle": "_calendar"
  },
  {
    "type": "get",
    "url": "/calendar/self",
    "title": "getTimeSpansForUser",
    "name": "getTimeSpansForUser",
    "group": "_calendar",
    "description": "<p>Searches for all Timespan entries for the user with the given apiKey. This includes resource reservations initiated by this user, illness and vacation entries.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "TimeSpan",
            "optional": false,
            "field": "timespan",
            "description": "<p>JSON Objects with all timespans currently registered for the user.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n\t[\n   {\n     \"id\" : 76,\n     \"title\" : \"aaaa\",\n     \"description\" : \"default:aaaa[admin]\",\n     \"dateFrom\" : 1479164400000,\n     \"dateUntil\" : 1485817200000,\n     \"type\" : \"RESOURCE_RESERVATION\",\n     \"department\" : ...list of departments...\n   }\n ]",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>DepartmentToken or APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/CalendarService.java",
    "groupTitle": "_calendar"
  },
  {
    "type": "get",
    "url": "/companies/{id}",
    "title": "getCompany",
    "name": "getCompany",
    "group": "_company",
    "description": "<p>Returns the company with the given id.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Company",
            "optional": false,
            "field": "company",
            "description": "<p>JSON Object with the company of the given id.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n\t{\n   \"id\" : 1,\n   \"name\" : \"Default Company\",\n   \"description\" : \"\",\n   \"mainAddress\" : {\n   \"id\" : 7,\n   \"zipCode\" : \"00000\",\n   \"phone\" : \"00000-00000\",\n   \"fax\" : \"00000-00000\",\n   \"city\" : \"City of Admin\",\n   \"street\" : \"Street of Admins\"\n   ...many more\n }",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/CompanyService.java",
    "groupTitle": "_company"
  },
  {
    "type": "get",
    "url": "/search/departments",
    "title": "searchDepartments",
    "name": "searchDepartments",
    "group": "_company",
    "description": "<p>Searches all departments which contain the given phrase</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "phrase",
            "description": "<p>The search phrase used in the search.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Department",
            "optional": false,
            "field": "department",
            "description": "<p>JSON department Objects which contained the search phrase.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n[\n  {\n    \"id\" : 6,\n    \"address\" : {\n    \"id\" : 5,\n    \"zipCode\" : \"00000\",\n    \"phone\" : \"00000-00000\",\n    \"fax\" : \"00000-00000\",\n    \"city\" : \"City of Admin\",\n    \"street\" : \"Street of Admins\"\n    ...many more\n   }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/CompanyService.java",
    "groupTitle": "_company"
  },
  {
    "type": "get",
    "url": "/departments/{id}",
    "title": "getDepartment",
    "name": "getDepartment",
    "group": "_department",
    "description": "<p>Returns the department with the given id.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Department",
            "optional": false,
            "field": "company",
            "description": "<p>JSON Object with the department of the given id.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DepartmentService.java",
    "groupTitle": "_department"
  },
  {
    "type": "delete",
    "url": "/remove?apiKey={apiKey}&departmentToken={departmenttoken}&id={id}",
    "title": "deleteDocument",
    "name": "deleteDocument",
    "group": "_documents",
    "description": "<p>Deletes a document</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "id",
            "description": "<p>The id of the document to remove.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>department token of the department the document belongs to.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "OK",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "get",
    "url": "/id/{id}?apiKey={apiKey}",
    "title": "getDocumentById",
    "name": "getDocumentById",
    "group": "_documents",
    "description": "<p>returns the document with the given id</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "DocumentInfo",
            "optional": false,
            "field": "document",
            "description": "<p>JSON DocumentInfo-Object.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "get",
    "url": "/id/{id}/content?apiKey={apiKey}",
    "title": "getDocumentContent",
    "name": "getDocumentContent",
    "group": "_documents",
    "description": "<p>returns the content of the document with the given id</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "byte[]",
            "optional": false,
            "field": "Document",
            "description": "<p>content</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "get",
    "url": "/documents/{departmentToken}/groups?apiKey={apiKey}",
    "title": "getDocumentGroups",
    "name": "getDocumentGroups",
    "group": "_documents",
    "description": "<p>gets all document groups (metadata only) within the given department</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Document",
            "optional": false,
            "field": "documents",
            "description": "<p>JSON DocumentInfo-Objects with information about found documents.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "get",
    "url": "/documents/{departmentToken}/{group}/?apiKey={apiKey}",
    "title": "getDocuments",
    "name": "getDocuments",
    "group": "_documents",
    "description": "<p>gets all documents (metadata only) within the given department and document group.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "group",
            "description": "<p>The document group name within this department to look for documents.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Document",
            "optional": false,
            "field": "documents",
            "description": "<p>JSON DocumentInfo-Objects with information about found documents.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "get",
    "url": "/search/{departmentToken}/documentGroup?apiKey={apiKey}&phrase={phrase}",
    "title": "searchDocuments",
    "name": "searchDocuments",
    "group": "_documents",
    "description": "<p>Returns all the documents in the given department and document group matching the search phrase.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "phrase",
            "description": "<p>The searchstring to searh for.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "DocumentInfo",
            "optional": false,
            "field": "documents",
            "description": "<p>JSON DocumentInfo-Objects with information about found documents.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "put",
    "url": "/id/{id}?apiKey={apiKey}&name={name}&mimeType={mimeType}&tag={tag}&changes={changes}",
    "title": "setDocumentAttributes",
    "name": "setDocumentAttributes",
    "group": "_documents",
    "description": "<p>Changes different attributes of a document</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>The new name of the document.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "mimeType",
            "description": "<p>The new MIME-Type of the document.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "tag",
            "description": "<p>The new document tag.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "changes",
            "description": "<p>description of changes made.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "OK",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/DocumentService.java",
    "groupTitle": "_documents"
  },
  {
    "type": "post",
    "url": "/create/{departmentToken}/{group}?apiKey={apiKey}",
    "title": "createResource",
    "name": "createResource",
    "group": "_resources",
    "description": "<p>creates a new resource</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "group",
            "description": "<p>The resource group to put new resource in.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "uuid",
            "description": "<ul> <li>uuid of new resource</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "name",
            "description": "<ul> <li>name of new resource</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "description",
            "description": "<ul> <li>description</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "slots",
            "description": "<ul> <li>max.number of slots for new device</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "ip",
            "description": "<ul> <li>ip address of new device/resource (if applicable)</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "commands",
            "description": "<ul> <li>list of commands the device understands</li> </ul>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "isAgent",
            "description": "<ul> <li>is device an agent?</li> </ul>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "200",
            "description": "<p>OK.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "delete",
    "url": "/remove/id/{id}?apiKey={apiKey}&departmentToken={departmenttoken}",
    "title": "deleteResourceById",
    "name": "deleteResourceById",
    "group": "_resources",
    "description": "<p>Deletes a resource by id</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "id",
            "description": "<p>The id of the resource to remove.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>department token of the department the resource belongs to.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "OK",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "delete",
    "url": "/remove/uuid/{uuid}?apiKey={apiKey}&departmentToken={departmenttoken}",
    "title": "deleteResourceByUuid",
    "name": "deleteResourceByUuid",
    "group": "_resources",
    "description": "<p>Deletes a resource by uuid</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "uuid",
            "description": "<p>The uuid of the resource to remove.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>department token of the department the resource belongs to.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "OK",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "get",
    "url": "/id/{id}?apiKey={apiKey}",
    "title": "getResourceById",
    "name": "getResourceById",
    "group": "_resources",
    "description": "<p>returns the resource/device with the given id</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Resource",
            "optional": false,
            "field": "resource/device",
            "description": "<p>Object.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "get",
    "url": "/list/{departmentToken}/groups?apiKey={apiKey}",
    "title": "getResourceGroups",
    "name": "getResourceGroups",
    "group": "_resources",
    "description": "<p>gets all resource groups within the given department.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "List",
            "description": "<p>of {ResourceGroup} objects with information about found resource groups.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "get",
    "url": "/list/{departmentToken}/{group}?apiKey={apiKey}",
    "title": "getResources",
    "name": "getResources",
    "group": "_resources",
    "description": "<p>gets all resources within the given department and group.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "departmentToken",
            "description": "<p>The department token of the department.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "group",
            "description": "<p>The resource group of the resources.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "List",
            "description": "<p>of {Resource} objects.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "put",
    "url": "/id/{id}?apiKey={apiKey}&name={name}&description={description}&mqttOnline={mqttOnline}&commands={commands}&geo=[geo}&set={set}",
    "title": "updateResource",
    "name": "updateResource",
    "group": "_resources",
    "description": "<p>Changes different attributes of a resource</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>The new name of the resource. omit if no changes.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>The new description of the resource. omit if no changes.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "mqttOnline",
            "description": "<p>(true/false) - Set the resources online state. omit if no changes.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "commands",
            "description": "<p>update command set the resource understands. separate by colon (e.G ON:OFF:RESET)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "geo",
            "description": "<p>new coordinates of the resource (LAT:LONG)- leave blank if no changes</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "set",
            "description": "<p>set a specific resource attribute to a specific value (e.G. NAME:WERT)</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "OK",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/ResourceService.java",
    "groupTitle": "_resources"
  },
  {
    "type": "get",
    "url": "/users/{id}",
    "title": "getUserById",
    "name": "getUserById",
    "group": "_users",
    "description": "<p>Returns user with the given {id}</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "User",
            "optional": false,
            "field": "JSON-Object",
            "description": "<p>with the user with id {id}.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "   HTTP/1.1 200 OK\n{\n \"id\": 48,\n \"name\": \"peter\",\n \"username\": \"peter\"\n}",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/UserRoleService.java",
    "groupTitle": "_users"
  },
  {
    "type": "get",
    "url": "/users/username/{username}",
    "title": "getUserByUsername",
    "name": "getUserByUsername",
    "group": "_users",
    "description": "<p>Returns user with the given {username}</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "User",
            "optional": false,
            "field": "JSON-Object",
            "description": "<p>with the user with username {username}.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "   HTTP/1.1 200 OK\n{\n \"id\": 48,\n \"name\": \"peter\",\n \"username\": \"peter\"\n}",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/UserRoleService.java",
    "groupTitle": "_users"
  },
  {
    "type": "get",
    "url": "/users/department/{departmentToken}",
    "title": "getUsers",
    "name": "getUsers",
    "group": "_users",
    "description": "<p>Returns all users within the department with token {departmentToken}</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "apiKey",
            "description": "<p>The API-Key of the user accessing the service.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "JSON-Array",
            "description": "<p>with all users in this department.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n[\n {\n  \"id\": 48,\n  \"name\": \"peter\",\n  \"username\": \"peter\"\n },\n {\n  \"id\": 54,\n  \"name\": \"torsten\",\n  \"username\": \"torsten\"\n }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "NotAuthorized",
            "description": "<p>APIKey incorrect.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./ejbModule/de/hallerweb/enterprise/prioritize/view/boundary/UserRoleService.java",
    "groupTitle": "_users"
  }
] });