idea:
    gradle idea

libs:
    vi gradle/libs.versions.toml

publish:
    gradle clean publishToMavenLocal --rerun-tasks

test args="":
    gradle clean test --rerun-tasks --info {{args}}

