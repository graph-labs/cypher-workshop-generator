import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute


def path = Paths.get("${request.getOutputDirectory()}/${request.getArtifactId()}/generate-importable-exercises.sh")
def permissions = new HashSet<PosixFilePermission>()
permissions.add(OWNER_READ)
permissions.add(OWNER_WRITE)
permissions.add(OWNER_EXECUTE)
permissions.add(GROUP_READ)
permissions.add(GROUP_WRITE)
permissions.add(GROUP_EXECUTE)
Files.createFile(path, asFileAttribute(permissions))

def file = path.toFile()
file.write """
    #!/usr/bin/env bash
    set -euo pipefail

    echo -n Neo4j password:
    read -s password
    mvn -q net.biville.florent.cypher:workshop-exporter-plugin:generate-cypher-file \
        -Dbolt-uri="bolt://localhost:7687" \
        -Dusername="neo4j" \
        -Dpassword="\$password" \
        -Dexercise-input=src/main/resources/exercises/exercises.json \
        -Dcypher-output=src/main/resources/exercises/dump.cypher
""".stripLeading()