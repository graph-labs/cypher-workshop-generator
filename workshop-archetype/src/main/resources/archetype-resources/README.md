# Cypher workshop

## Getting started

 1. make sure the credentials are correct in `generate-importable-exercises.sh` (piece of advice: use git-crypt or read password from env)
 1. run `generate-importable-exercises.sh`
 1. build the project `mvn package`
 1. run the REPL by decompressing the archive and running the packaged script, e.g.
```shell
 $ cd target
 $ unzip cypher-workshop.zip
 $ ./cypher-workshop/bin/cypher-workshop -p
[[[ interactively prompts for your Neo4j password ]]]
```

## Defining exercises

It is as easy as changing `src/main/resources/exercises.json`
and re-running `generate-importable-exercises.sh`.