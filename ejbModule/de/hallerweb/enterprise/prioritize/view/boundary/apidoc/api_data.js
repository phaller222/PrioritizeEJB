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
    "filename": "./apidoc/main.js",
    "group": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
    "groupTitle": "C__Entwicklung_projekteStudio_PrioritizeEJB_ejbModule_de_hallerweb_enterprise_prioritize_view_boundary_apidoc_main_js",
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
    "filename": "./CalendarService.java",
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
    "filename": "./CalendarService.java",
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
    "filename": "./CompanyService.java",
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
    "filename": "./CompanyService.java",
    "groupTitle": "_company"
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
          "content": "    HTTP/1.1 200 OK\n [\n   {\n\t\t\"id\" : 80,\n\t\t\"currentDocument\" : {\n\t\t\t\"id\" : 79,\n\t\t\t\"name\" : \"testdefault\",\n\t\t\t\"version\" : 1,\n\t\t\t\"mimeType\" : \"image/png\",\n\t\t\t\"tag\" : null,\n\t\t\t\"lastModified\" : 1485693706000,\n\t\t\t\"encrypted\" : false,\n\t\t\t\"changes\" : \"\",\n\t\t\t\"lastModifiedBy\" : {\n\t\t\t\t\"id\" : 18,\n\t\t\t\t\"name\" : \"admin\",\n\t\t\t\t\"username\" : \"admin\",\n\t\t\t\t\"assignedTasks\" : [ ]\n\t\t\t},\n\t\t\t\"encryptedBy\" : null\n\t\t},\n\t\t\"recentDocuments\" : [ ],\n\t\t\"locked\" : false,\n\t\t\"lockedBy\" : null,\n\t }\n]",
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
    "filename": "./DocumentService.java",
    "groupTitle": "_documents"
  }
] });