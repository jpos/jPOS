idea:
    gradle idea

libs:
    vi gradle/libs.versions.toml

publish:
    gradle clean publishToMavenLocal --rerun-tasks

test args="":
    gradle clean test --rerun-tasks --info {{args}}

metrics:
    curl http://localhost:18583/metrics

[no-cd]
lint:
    gradle -Plint clean jar

