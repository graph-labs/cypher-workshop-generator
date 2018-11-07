# Cypher workshop generator

## Build status

[![Build Status](https://travis-ci.org/fbiville/cypher-workshop-generator.png?branch=master)](https://travis-ci.org/fbiville/cypher-workshop-generator)

## Generate a workshop

### For manual testing

```
 $ mvn install
 $ cd workshop-archetype
 $ ./generate-test-workshop.sh
```

### Fo' real

```
 $ mvn install
 $ cd /wherever/you/want
 $ mvn archetype:generate -DarchetypeCatalog=local
 # select the archetype in the (hopefully) short list
 # interactively fill in the required details
```

Once the workshop base is generated, just check out its `README` file.
