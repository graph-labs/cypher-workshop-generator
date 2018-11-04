#!/usr/bin/env bash

set -euo pipefail

parse_pom_property() {
    property_name=$1
    mvn -q help:evaluate -Dexpression=${property_name} -DforceStdout
}

echo -n "Extracting archetype metadata"

archetype_group_id=$(parse_pom_property project.groupId)

archetype_artifact_id=$(parse_pom_property project.artifactId)

archetype_version=$(parse_pom_property project.version)

echo -n "........................ DONE !"

echo
echo -n "Installing archetype"

mvn -q clean install &> /dev/null

echo -n "................................. DONE !"


echo
echo -n "Generating test workshop instance"

workshop_test_directory=$(mktemp -d)

cd ${workshop_test_directory}

mvn -q archetype:generate -B \
    -DgroupId=toto \
    -DartifactId=toto \
    -DarchetypeCatalog=local \
    -DarchetypeGroupId=${archetype_group_id} \
    -DarchetypeArtifactId=${archetype_artifact_id} \
    -DarchetypeVersion=${archetype_version} &> /dev/null

echo -n ".................... DONE !"
echo
echo "See result in: ${workshop_test_directory}/toto"
