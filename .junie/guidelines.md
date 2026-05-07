* When working with `teamcity app {url}` - always start the URL with '/'

* CI/CD is configured on https://artem-demo.teamcity.com/ 

* When use TeamCity Pipelines for the configuration, here is the YAML json schema:
```yaml
{
  "title": "Pipeline schema",
  "description": "Main pipeline definition.",
  "required": [
    "jobs"
  ],

  "definitions": {

      "dependencies": {
        "type": "array",
        "description": "A collection of dependencies that represent pipeline relations with external objects (classic build configurations or other pipelines).",
        "items": {
          "anyOf": [
            {
              "type": "string",
              "description": "A simple dependency that references a linked object via its external ID.",
              "pattern": "^[A-Za-z0-9_]+$"
            },
            {
              "type": "object",
              "description": "A dependency with additional properties that specify the behavior of linked objects.",
              "patternProperties": {
                "^[A-Za-z0-9_]+$": {
                  "properties": {
                    "reuse": {
                      "type": "string",
                      "enum": [
                        "none",
                        "successful",
                        "successful-or-failed"
                      ],
                      "description": "Specifies which of upstream dependency builds (runs) can be reused."
                    },
                    "enforce-revisions-synchronisation": {
                      "type": "boolean",
                      "description": "Specifies whether the code revisions should be explicitly synchronized."
                    },
                    "on-failed-dependency": {
                      "type": "string",
                      "enum": [
                        "run-add-problem",
                        "run-ignore-problem",
                        "mark-as-failed-to-start",
                        "cancel"
                      ],
                      "description": "Specifies the default action in case an upstream dependency fails."
                    },
                    "on-incomplete-dependency": {
                      "type": "string",
                      "enum": [
                        "run-add-problem",
                        "run-ignore-problem",
                        "mark-as-failed-to-start",
                        "cancel"
                      ],
                      "description": "Specifies the default action in case an upstream dependency fails to start."
                    }
                  },
                  "additionalProperties": false
                }
              }
            }
          ]
        }
    },

    "files": {
      "title": "Files publication",
      "description": "The collection of job file outputs, including both files published as artifacts and files shared to downstream jobs.",
      "type": "string",
      "enum": [
        ""
      ]
    },
    "runOn": {
      "title": "Agents",
      "description": "The collection of agents eligible to run this job.",
      "definitions": {
        "arch": {
          "type": "object",
          "title": "CPU Architecture",
          "properties": {
            "arch": {
              "type": "string",
              "description": "The architechture of the agent machine.",
              "enum": [
                "x86",
                "x86_64",
                "arm",
                "aarch64",
                "ia64",
                "sparc",
                "riscv64"
              ]
            }
          },
          "additionalProperties": false
        },
        "cpu": {
          "type": "object",
          "title": "CPU count",
          "properties": {
            "cpu": {
              "type": "integer",
              "description": "The number of CPU cores on the agent machine."
            }
          },
          "additionalProperties": false
        },
        "custom": {
          "type": "object",
          "title": "A user-defined expression that TeamCity evaluates for each authorized agent. Agents that fit the criteria are marked as eligible to run the job, others are labeled as incompatible.",
          "properties": {
            "name": {
              "description": "The display name of a custom requirement."
            },
            "requirement": {
              "type": "string",
              "description": "The logical operator used to compare the actual agent parameter value with the given one.",
              "enum": [
                "exists",
                "not-exists",
                "contains",
                "matches",
                "equals",
                "not-equals",
                "more-than",
                "less-than",
                "more-than-or-equals",
                "less-than-or-equals"
              ]
            },
            "parameter": {
              "description": "A parameter reported by the agent machine whose value must match the required criteria."
            },
            "value": {
              "description": "A custom value to compare against the agent's parameter value. Not required for the 'exists' operator."
            }
          },
          "required": ["requirement", "parameter", "name"],
          "additionalProperties": false
        },
        "os-version": {
          "type": "object",
          "title": "Operating system version",
          "description": "The operating system version of the agent machine.",
          "properties": {
            "os-version": {
              "type": "string"
            }
          },
          "additionalProperties": false
        },
        "os-family": {
          "type": "object",
          "title": "Operating system family",
          "description": "The operating system family.",
          "properties": {
            "os-family": {
              "type": "string"
            }
          },
          "additionalProperties": false
        },
        "ram": {
          "type": "object",
          "title": "RAM size",
          "description": "The amount of memory installed on the agent machine.",
          "properties": {
            "ram": {
              "type": "string",
              "pattern": "^\\d+GB$"
            }
          },
          "additionalProperties": false
        }
      },
      "anyOf": [
        {
          "type": "string",
          "title": "JetBrains-hosted agents",
          "description": "Build agents hosted and maintained by JetBrains.",
          "enum": [
            "Windows-Small",
            "Mac-Medium",
            "Linux-XLarge",
            "Linux-Large",
            "Linux-Medium",
            "Linux-Small",
            "Windows-Medium"
          ]
        },
        {
          "type": "string",
          "title": "Self-hosted agents",
          "description": "Self-hosted build agents maintained by your agent administrators.",
          "enum": [
            "self-hosted"
          ]
        },
        {
          "type": "object",
          "title": "Self-hosted agents",
          "description": "Self-hosted build agents maintained by your agent administrators.",
          "properties": {
            "self-hosted": {
              "type": "array",
              "items": {
                "anyOf": [
                  {"$ref": "#/definitions/runOn/definitions/arch"},
                  {"$ref": "#/definitions/runOn/definitions/cpu"},
                  {"$ref": "#/definitions/runOn/definitions/os-family"},
                  {"$ref": "#/definitions/runOn/definitions/os-version"},
                  {"$ref": "#/definitions/runOn/definitions/ram"},
                  {"$ref": "#/definitions/runOn/definitions/custom"}
                ]
              }
            }
          },
          "additionalProperties": false
        }
      ]
    },
    "repositories": {
      "type": "object",
      "description": "A collection of remote repositories this job can check out when it runs.",
      "properties": {
        "main": {
          "type": "object",
          "description": "The main repository that cannot be removed.",
          "properties": {
            "enabled": {
              "type": "boolean"
            },
            "path": {
              "type": "string"
            }
          },
          "additionalProperties": false
        }
      },
      "patternProperties": {
        "^[A-Za-z0-9_]+$": {
          "$ref": "#/definitions/repositories/properties/main"
        },
        "^https?://.+$": {
          "$ref": "#/definitions/repositories/properties/main"
        }
      },
      "additionalProperties": false
    },
    "job": {
      "type": "object",
      "description": "An individual job that specifies the sequence of basic build steps, defines its own input parameters, and has its own build agent requirements.",
      "properties": {
        "name": {
          "type": "string"
        },
        "parameters": {
          "type": "object",
          "title": "Job input parameters",
          "description": "The list of name/value pairs designed to be used only within its parent job. Can be referenced directly by name (%env.GIT_HOME%).",
          "additionalProperties": {
            "type": "string"
          }
        },
        "output-parameters": {
          "type": "object",
          "title": "Job output parameters",
          "description": "Deprecated: All job parameters are now automatically available to dependent jobs within the same pipeline via '%job.jobId.parameterName%' syntax.",
          "deprecated": true,
          "additionalProperties": {
            "type": "string"
          }
        },
        "steps": {
          "title": "Step",
          "description": "The collection of build steps in the order of their execution.",
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "type": "string",
                "enum": [
                  "script",
                  "maven",
                  "gradle",
                  "node-js"
                ]
              }
            }
          }
        },
        "features": {
          "title": "Features",
          "description": "The collection of build features.",
          "type": "array",
          "items": {
            "type": "object",
            "properties": { }
          }
        },
        "files-publication": {
          "type": "array",
          "title": "Files publication",
          "description": "The collection of job file outputs, including both files published as artifacts and files shared to downstream jobs.",
          "items": {
            "anyOf": [
              {
                "type": "string"
              },
              {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string"
                  },
                  "share-with-jobs": {
                    "type": "boolean",
                    "description": "true if dependent downstream jobs can access this file; otherwise, false."
                  },
                  "publish-artifact": {
                    "type": "boolean",
                    "description": "true if this file should be visible on the Artifacts tab of the run results page; otherwise, false."
                  }
                },
                "required": ["path"],
                "additionalProperties": false
              }
            ]
          }
        },
        "runs-on": {
          "$ref": "#/definitions/runOn"
        },
        "files": {
          "$ref": "#/definitions/files"
        },
        "dependencies": {
          "type": "array",
          "items": {
            "anyOf": [
              {
                "type": "string"
              },
              {
                "type": "object",
                "properties": {}
              }
            ]
          }
        },
        "integrations": {
          "type": "array",
          "description": "The list of credentials required to access external resources, such as Docker or NPM registries.",
          "items": {
            "anyOf": [
              {
                "type": "string"
              },
              {
                "type": "object",
                "properties": {}
              }
            ]
          }
        },
        "repositories": {
          "type": "array",
          "items": {
            "$ref": "#/definitions/repositories"
          }
        },
        "parallelism": {
          "type": "number",
          "description": "The number of batches in which TeamCity should attempt to split the tests."
        },
        "checkout-working-directories-only": {
          "type": "boolean",
          "deprecated": true
        },
        "enable-dependency-cache": {
          "type": "boolean"
        },
        "allow-reuse": {
          "type": "boolean",
          "description": "true, if TeamCity should reuse the previous run if neither the job nor a remote repository has any new changes; false, if a job should run anew every time it's triggered."
        },
        "download-artifacts": {
          "type": "array",
          "title": "Downloaded non-pipeline artifacts",
          "description": "A collection of artifact dependencies from external build configurations outside the pipeline's direct dependency chain.",
          "items": {
            "type": "object",
            "patternProperties": {
              "^[A-Za-z0-9_]+$": {
                "type": "object",
                "properties": {
                  "from": {
                    "description": "Specifies which build to retrieve artifacts from.",
                    "oneOf": [
                      {
                        "type": "string",
                        "enum": [
                          "last-finished",
                          "last-successful",
                          "last-pinned",
                          "dependency",
                          "dependency-or-last-finished"
                        ]
                      },
                      {
                        "type": "object",
                        "properties": {
                          "tagged": {
                            "type": "string",
                            "description": "Build with a specific tag."
                          },
                          "build-number": {
                            "type": "string",
                            "description": "Specific build number."
                          },
                          "last-pinned": {
                            "type": "boolean",
                            "description": "Most recently pinned build."
                          },
                          "dependency": {
                            "type": "boolean",
                            "description": "Build from the same chain."
                          },
                          "dependency-or-last-finished": {
                            "type": "boolean",
                            "description": "Build from the same chain or most recently finished build."
                          },
                          "last-finished": {
                            "type": "boolean",
                            "description": "Most recently finished build."
                          },
                          "last-successful": {
                            "type": "boolean",
                            "description": "Most recently successful build."
                          },
                          "branch": {
                            "type": "string",
                            "description": "Filter by branch pattern."
                          }
                        },
                        "additionalProperties": false
                      }
                    ]
                  },
                  "artifact-rules": {
                    "type": "string",
                    "description": "Defines which artifacts to download."
                  },
                  "clean-destination": {
                    "type": "boolean",
                    "description": "When true, clears the destination directory before downloading artifacts."
                  }
                },
                "required": ["from", "artifact-rules"],
                "additionalProperties": false
              }
            },
            "additionalProperties": false
          }
        }
      },
      "additionalProperties": false
    }
  },
  "properties": {
    "name": {
      "title": "Name of the pipeline",
      "description": "The public pipeline name and ID.",
      "type": "string",
      "deprecated": true
    },
    "parameters": {
      "type": "object",
      "description": "The list of name/value pairs that each job can access via the '%parameter-name%' syntax.",
      "additionalProperties": {
        "type": "string"
      }
    },
    "secrets": {
      "type": "object",
      "description": "The list parameters that store sensitive values and never expose them via the UI or public API.",
      "additionalProperties": {
        "type": "string",
        "pattern": "^credentialsJSON:.+"
      }
    },
    "output-parameters": {
      "type": "object",
      "title": "Pipeline output parameters",
      "description": "The list of name/value pairs that expose parameters to downstream pipelines and build configurations. Values can reference job parameters (%job.jobId.paramName%) and pipeline parameters (%paramName%), including parameters inherited from parent projects.",
      "additionalProperties": {
        "type": "string"
      }
    },
    "jobs": {
      "type": "object",
      "description": "The list of jobs owned by this pipeline",
      "properties": {},
      "patternProperties": {
        "^[^\\s]+$": {
          "$ref": "#/definitions/job"
        }
      },
      "additionalProperties": {
        "$ref": "#/definitions/job"
      }
    },
    "dependencies": {
      "$ref": "#/definitions/dependencies"
    },
    "import-parameters": {
      "type": "array",
      "title": "Import Parameters",
      "description": "The list of parameters that should be imported from parent TeamCity projects.",
      "items": {
        "type": "string"
      }
    }
  },
  "additionalProperties": false
}
```

And separately schemas for supported build step types:
```
{
  "maven": {
    "jsonSchema": {
      "$schema": "https://json-schema.org/draft/2020-12/schema",
      "type": "object",
      "properties": {
        "goals": {
          "type": "string",
          "description": "Space-separated list of goals to execute"
        },
        "pom-location": {
          "type": "string",
          "description": "The path (relative to the checkout directory) to a Maven POM file.",
          "default": "pom.xml"
        },
        "runner-arguments": {
          "type": "string",
          "description": "Additional Maven command line parameters."
        },
        "working-directory": {
          "type": "string",
          "description": "\n        The custom working directory for Maven. If not specified, the checkout directory is used.\n      "
        },
        "maven-version": {
          "oneOf": [
            {
              "type": "object",
              "properties": {
                "auto": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "auto"
              ],
              "title": "Auto",
              "description": "\n          Maven version specified by the M2_HOME environment variable.\n          If the environment variable is empty, then the default Maven version provided by the TeamCity server will be used.\n        "
            },
            {
              "type": "object",
              "properties": {
                "defaultProvidedVersion": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "defaultProvidedVersion"
              ],
              "title": "Default provided version",
              "description": "The default Maven version provided by TeamCity server"
            },
            {
              "type": "object",
              "properties": {
                "custom": {
                  "type": "object",
                  "properties": {
                    "path": {
                      "type": "string",
                      "description": "The path to a custom Maven installation"
                    }
                  },
                  "required": []
                }
              },
              "required": [
                "custom"
              ],
              "title": "Custom",
              "description": "The custom Maven version found at the specified path"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-0": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-0"
              ],
              "title": "Bundled 3 0",
              "description": "Use maven 3.0.5 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-1": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-1"
              ],
              "title": "Bundled 3 1",
              "description": "Use maven 3.1.1 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-2": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-2"
              ],
              "title": "Bundled 3 2",
              "description": "Use maven 3.2.5 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-3": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-3"
              ],
              "title": "Bundled 3 3",
              "description": "Use maven 3.3.9 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-5": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-5"
              ],
              "title": "Bundled 3 5",
              "description": "Use maven 3.5.4 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-6": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-6"
              ],
              "title": "Bundled 3 6",
              "description": "Use maven 3.6.3 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-8": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-8"
              ],
              "title": "Bundled 3 8",
              "description": "Use maven 3.8.6 bundled with TeamCity"
            },
            {
              "type": "object",
              "properties": {
                "bundled-3-9": {
                  "type": "object",
                  "properties": {},
                  "required": []
                }
              },
              "required": [
                "bundled-3-9"
              ],
              "title": "Bundled 3 9",
              "description": "Use maven 3.9.11 bundled with TeamCity"
            }
          ],
          "unevaluatedProperties": false,
          "title": "Maven version",
          "description": "Maven version to use"
        },
        "user-settings-selection": {
          "type": "string",
          "description": "Use one of the predefined settings files or provide a custom path. By default, the standard Maven settings file location is used.",
          "default": "userSettingsSelection:default"
        },
        "user-settings-path": {
          "type": "string",
          "description": "The path to a user settings file"
        },
        "local-repo-scope": {
          "type": "string",
          "oneOf": [
            {
              "const": "maven-default",
              "title": "Maven default",
              "description": "\n          Shared by all build configurations and all agents on the machine.\n        "
            },
            {
              "const": "agent",
              "title": "Agent",
              "description": "\n          Shared by all the builds on the agent. Can be cleaned by an agent to free disk space.\n        "
            },
            {
              "const": "build-configuration",
              "title": "Build configuration",
              "description": "\n          Shared by all the builds in a single build configuration. Can be cleaned by an agent to free disk space.\n        "
            }
          ],
          "default": "agent",
          "title": "Local repo scope"
        },
        "is-incremental": {
          "type": "boolean",
          "description": "Enable incremental building"
        },
        "jdk-home": {
          "type": "string",
          "description": "\n        A path to [JDK](https://www.jetbrains.com/help/teamcity/?Predefined+Build+Parameters#PredefinedBuildParameters-DefiningJava-relatedEnvironmentVariables) to use.\n        By default, the JAVA_HOME environment variable or the agent\u0027s own Java is used.\n      "
        },
        "jvm-args": {
          "type": "string",
          "description": "\n        The space-separated list of additional arguments for JVM\n      "
        },
        "coverage-engine": {
          "oneOf": [
            {
              "type": "object",
              "properties": {
                "idea": {
                  "type": "object",
                  "properties": {
                    "include-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be analyzed by code coverage.\n            A pattern should start with a valid package name and can contain a wildcard, for example: org.apache.*\n          "
                    },
                    "exclude-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be excluded from the coverage. Exclude patterns have priority over include patterns.\n          "
                    }
                  },
                  "required": []
                }
              },
              "required": [
                "idea"
              ],
              "title": "Idea"
            },
            {
              "type": "object",
              "properties": {
                "jacoco": {
                  "type": "object",
                  "properties": {
                    "class-locations": {
                      "type": "string",
                      "description": "\n            Newline-delimited set of path patterns in the form of +|-:path to scan for classfiles to be analyzed.\n            Excluding libraries and test classes from analysis is recommended. Ant like patterns are supported.\n          "
                    },
                    "exclude-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be excluded from the coverage.\n            Exclude patterns have priority over include patterns.\n          "
                    },
                    "jacoco-version": {
                      "type": "string",
                      "description": "JaCoCo version to use"
                    }
                  },
                  "required": []
                }
              },
              "required": [
                "jacoco"
              ],
              "title": "Jacoco"
            }
          ],
          "unevaluatedProperties": false,
          "title": "Coverage engine",
          "description": "\n        Specifies coverage engine to use\n      "
        },
        "kubernetes-pull-policy": {
          "type": "string",
          "oneOf": [
            {
              "const": "if-not-present",
              "title": "If not present"
            },
            {
              "const": "always",
              "title": "Always"
            },
            {
              "const": "never",
              "title": "Never"
            }
          ],
          "default": "if-not-present",
          "title": "Kubernetes pull policy",
          "description": "\n        Specifies which pull policy will be used with the specified image at each pod\u0027s execution.\n      "
        },
        "docker-image": {
          "type": "string",
          "description": "\n        Specifies which Docker image to use for running this build step. I.e. the build step will be run inside specified docker image, using \u0027docker run\u0027 wrapper.\n      "
        },
        "docker-image-platform": {
          "type": "string",
          "oneOf": [
            {
              "const": "any",
              "title": "Any"
            },
            {
              "const": "linux",
              "title": "Linux"
            },
            {
              "const": "windows",
              "title": "Windows"
            }
          ],
          "title": "Docker image platform",
          "description": "\n        Specifies which Docker image platform will be used to run this build step.\n      "
        },
        "docker-pull": {
          "type": "boolean",
          "description": "\n        If enabled, \"docker pull [image][dockerImage]\" will be run before docker run.\n      "
        },
        "docker-run-parameters": {
          "type": "string",
          "description": "\n        Additional docker run command arguments\n      "
        }
      },
      "required": [],
      "title": "maven",
      "description": "\n      A [build step](https://www.jetbrains.com/help/teamcity/?Maven) running Maven\n    "
    }
  },
  "script": {
    "jsonSchema": {
      "$schema": "https://json-schema.org/draft/2020-12/schema",
      "type": "object",
      "properties": {
        "working-dir": {
          "type": "string",
          "description": "\n        [Build working directory](https://www.jetbrains.com/help/teamcity/?Build+Working+Directory) for script,\n        specify it if it is different from the [checkout directory](https://www.jetbrains.com/help/teamcity/?Build+Checkout+Directory).\n      "
        },
        "script-content": {
          "type": "string",
          "description": "\n        Content of the script to run\n      "
        },
        "format-stderr-as-error": {
          "type": "boolean",
          "description": "\n        Log stderr output as errors in the build log\n      "
        },
        "kubernetes-pull-policy": {
          "type": "string",
          "oneOf": [
            {
              "const": "if-not-present",
              "title": "If not present"
            },
            {
              "const": "always",
              "title": "Always"
            },
            {
              "const": "never",
              "title": "Never"
            }
          ],
          "default": "if-not-present",
          "title": "Kubernetes pull policy",
          "description": "\n        Specifies which pull policy will be used with the specified image at each pod\u0027s execution.\n      "
        },
        "docker-image": {
          "type": "string",
          "description": "\n        Specifies which Docker image to use for running this build step. I.e. the build step will be run inside specified docker image, using \u0027docker run\u0027 wrapper.\n      "
        },
        "docker-image-platform": {
          "type": "string",
          "oneOf": [
            {
              "const": "any",
              "title": "Any"
            },
            {
              "const": "linux",
              "title": "Linux"
            },
            {
              "const": "windows",
              "title": "Windows"
            }
          ],
          "title": "Docker image platform",
          "description": "\n        Specifies which Docker image platform will be used to run this build step.\n      "
        },
        "docker-pull": {
          "type": "boolean",
          "description": "\n        If enabled, \"docker pull [image][dockerImage]\" will be run before docker run.\n      "
        },
        "docker-run-parameters": {
          "type": "string",
          "description": "\n        Additional docker run command arguments\n      "
        }
      },
      "required": [
        "script-content"
      ],
      "title": "script",
      "description": "\n      A [build step](https://www.jetbrains.com/help/teamcity/?Command+Line) running a script with the specified content\n    "
    }
  },
  "gradle": {
    "jsonSchema": {
      "$schema": "https://json-schema.org/draft/2020-12/schema",
      "type": "object",
      "properties": {
        "tasks": {
          "type": "string",
          "description": "Space-separated task names. TeamCity runs the \u0027default\u0027 task if this field is empty."
        },
        "build-file": {
          "type": "string",
          "description": "\n        The path to a custom Gradle build file. Leave this field empty if your build file is build.gradle located in the root directory.\n        This property is deprecated for Gradle versions 9.0 and higher, use the additional `-p \u003cpath-relative-to-checkout-directory\u003e` command line parameter instead.\n      "
        },
        "incremental": {
          "type": "boolean",
          "description": "Enable this option to allow TeamCity to detect Gradle modules affected by a modified build, and run the :buildDependents only for these affected modules."
        },
        "working-directory": {
          "type": "string",
          "description": "Custom working directory for the Gradle script"
        },
        "gradle-home": {
          "type": "string",
          "description": "The path to a custom Gradle version. This version will be used instead of the default Gradle version referenced by the GRADLE_HOME environment variable."
        },
        "gradle-params": {
          "type": "string",
          "description": "Optional space-separated command-line parameters"
        },
        "use-gradle-wrapper": {
          "type": "string",
          "description": "Allowed values: [\"true\"|\"false\"]. Enable this setting if TeamCity should look for a [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) script in the project directory.",
          "default": true
        },
        "gradle-wrapper-path": {
          "type": "string",
          "description": "The path (relative to the working directory) to a Gradle Wrapper script"
        },
        "enable-debug": {
          "type": "boolean",
          "description": "\n        Runs Gradle with the \u0027debug\u0027 (-d) log level. See also: [Logging Sensitive Information](https://docs.gradle.org/current/userguide/logging.html#sec:debug_security).\n      "
        },
        "enable-stacktrace": {
          "type": "string",
          "description": "\n        Allows Gradle to print [truncated stacktraces](https://docs.gradle.org/current/userguide/logging.html#stacktraces).\n      "
        },
        "jdk-home": {
          "type": "string",
          "description": "\n        Custom [JDK](https://www.jetbrains.com/help/teamcity/?Predefined+Build+Parameters#PredefinedBuildParameters-DefiningJava-relatedEnvironmentVariables) to use.\n        The default is JAVA_HOME environment variable or the agent\u0027s own Java.\n      "
        },
        "jvm-args": {
          "type": "string",
          "description": "\n        Space-separated list of additional arguments for JVM\n      "
        },
        "coverage-engine": {
          "oneOf": [
            {
              "type": "object",
              "properties": {
                "idea": {
                  "type": "object",
                  "properties": {
                    "include-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be analyzed by code coverage.\n            A pattern should start with a valid package name and can contain a wildcard, for example: org.apache.*\n          "
                    },
                    "exclude-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be excluded from the coverage. Exclude patterns have priority over include patterns.\n          "
                    }
                  },
                  "required": []
                }
              },
              "required": [
                "idea"
              ],
              "title": "Idea"
            },
            {
              "type": "object",
              "properties": {
                "jacoco": {
                  "type": "object",
                  "properties": {
                    "class-locations": {
                      "type": "string",
                      "description": "\n            Newline-delimited set of path patterns in the form of +|-:path to scan for classfiles to be analyzed.\n            Excluding libraries and test classes from analysis is recommended. Ant like patterns are supported.\n          "
                    },
                    "exclude-classes": {
                      "type": "string",
                      "description": "\n            Newline-separated patterns for fully qualified class names to be excluded from the coverage.\n            Exclude patterns have priority over include patterns.\n          "
                    },
                    "jacoco-version": {
                      "type": "string",
                      "description": "JaCoCo version to use"
                    }
                  },
                  "required": []
                }
              },
              "required": [
                "jacoco"
              ],
              "title": "Jacoco"
            }
          ],
          "unevaluatedProperties": false,
          "title": "Coverage engine",
          "description": "\n        Specifies coverage engine to use\n      "
        },
        "kubernetes-pull-policy": {
          "type": "string",
          "oneOf": [
            {
              "const": "if-not-present",
              "title": "If not present"
            },
            {
              "const": "always",
              "title": "Always"
            },
            {
              "const": "never",
              "title": "Never"
            }
          ],
          "default": "if-not-present",
          "title": "Kubernetes pull policy",
          "description": "\n        Specifies which pull policy will be used with the specified image at each pod\u0027s execution.\n      "
        },
        "docker-image": {
          "type": "string",
          "description": "\n        Specifies which Docker image to use for running this build step. I.e. the build step will be run inside specified docker image, using \u0027docker run\u0027 wrapper.\n      "
        },
        "docker-image-platform": {
          "type": "string",
          "oneOf": [
            {
              "const": "any",
              "title": "Any"
            },
            {
              "const": "linux",
              "title": "Linux"
            },
            {
              "const": "windows",
              "title": "Windows"
            }
          ],
          "title": "Docker image platform",
          "description": "\n        Specifies which Docker image platform will be used to run this build step.\n      "
        },
        "docker-pull": {
          "type": "boolean",
          "description": "\n        If enabled, \"docker pull [image][dockerImage]\" will be run before docker run.\n      "
        },
        "docker-run-parameters": {
          "type": "string",
          "description": "\n        Additional docker run command arguments\n      "
        }
      },
      "required": [],
      "title": "gradle",
      "description": "\n      A [build step](https://www.jetbrains.com/help/teamcity/?Gradle) running gradle script\n    "
    }
  },
}
```

* To list available pipelines, make GET `app/rest/pipelines/parentProject:{parent_project_id}`. `-H "Accept: application/json"` header is required. Pipelines are NOT listed as subprojects or configurations of a project.

* To create a pipeline, make POST 'app/pipeline?parentProjectExtId={parent_project_ext_id}' endpoint. An example of the request body:
```
--data '{"name":"Name of the pipeline","notifications":[],"vcsRoot":{"externalVcsRootId":"VCS_root_external_id"},"additionalVcsRoots":[],"yaml":"jobs:\n  Job1:\n    name: Job 1\n    steps: []\n"}'
```

* To modify a pipeline, make POST 'app/pipeline/{pipeline_id}' endpoint. The request body should include the ID of the pipeline, including internalId in a form of 'projectXXX':
```yaml
{"id":"pipeline_external_id","internalId":"project123","name":"Name of the pipeline","pipelineVersion":"8b6262acca015657cdb0f3bb33e413ef","triggers":[],"versionedSettings":{"filename":null,"storedInRepo":false},"notifications":[],"vcsRoot":{"isPrivate":false,"branch":"main","vcsName":"jetbrains.git","isStatusPublishingEnabled":false,"connectionId":null,"isWebhookEnabled":false,"sshKey":null,"branchSpecification":"refs/heads/*","vcsProviderType":null,"trackPullRequests":false,"username":null,"url":"https://github.com/spring-projects/spring-petclinic","name":"Spring Petclinic repo","id":"VCS_root_extenal_id","isExternal":true,"externalVcsRootId":"VCS_root_extenal_id","statusPublishingEnabled":false,"pullRequestsTrackingEnabled":false},"additionalVcsRoots":[],"integrations":[],"yaml":"jobs:\n  Job1:\n    name: Job 1\n    steps: []\n  Job2:\n    name: Job 2\n    dependencies:\n      - Job1\n"}
```

* To trigger a pipeline run, use `tamcity api -X POST app/rest/buildQueue?fields=id` with request body `{"buildType":{"id":"pipeline_head_external_id"}}`. pipeline_head_external_id is `{pipeline_external_id}_PipelineHead`. 
