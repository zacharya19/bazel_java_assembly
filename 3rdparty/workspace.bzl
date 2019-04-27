# Do not edit. bazel-deps autogenerates this file from dependencies.yml.
def _jar_artifact_impl(ctx):
    jar_name = "%s.jar" % ctx.name
    ctx.download(
        output=ctx.path("jar/%s" % jar_name),
        url=ctx.attr.urls,
        sha256=ctx.attr.sha256,
        executable=False
    )
    src_name="%s-sources.jar" % ctx.name
    srcjar_attr=""
    has_sources = len(ctx.attr.src_urls) != 0
    if has_sources:
        ctx.download(
            output=ctx.path("jar/%s" % src_name),
            url=ctx.attr.src_urls,
            sha256=ctx.attr.src_sha256,
            executable=False
        )
        srcjar_attr ='\n    srcjar = ":%s",' % src_name

    build_file_contents = """
package(default_visibility = ['//visibility:public'])
java_import(
    name = 'jar',
    tags = ['maven_coordinates={artifact}'],
    jars = ['{jar_name}'],{srcjar_attr}
)
filegroup(
    name = 'file',
    srcs = [
        '{jar_name}',
        '{src_name}'
    ],
    visibility = ['//visibility:public']
)\n""".format(artifact = ctx.attr.artifact, jar_name = jar_name, src_name = src_name, srcjar_attr = srcjar_attr)
    ctx.file(ctx.path("jar/BUILD"), build_file_contents, False)
    return None

jar_artifact = repository_rule(
    attrs = {
        "artifact": attr.string(mandatory = True),
        "sha256": attr.string(mandatory = True),
        "urls": attr.string_list(mandatory = True),
        "src_sha256": attr.string(mandatory = False, default=""),
        "src_urls": attr.string_list(mandatory = False, default=[]),
    },
    implementation = _jar_artifact_impl
)

def jar_artifact_callback(hash):
    src_urls = []
    src_sha256 = ""
    source=hash.get("source", None)
    if source != None:
        src_urls = [source["url"]]
        src_sha256 = source["sha256"]
    jar_artifact(
        artifact = hash["artifact"],
        name = hash["name"],
        urls = [hash["url"]],
        sha256 = hash["sha256"],
        src_urls = src_urls,
        src_sha256 = src_sha256
    )
    native.bind(name = hash["bind"], actual = hash["actual"])


def list_dependencies():
    return [
    {"artifact": "commons-cli:commons-cli:1.4", "lang": "java", "sha1": "c51c00206bb913cd8612b24abd9fa98ae89719b1", "sha256": "fd3c7c9545a9cdb2051d1f9155c4f76b1e4ac5a57304404a6eedb578ffba7328", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4.jar", "source": {"sha1": "40dfd9fdef125e19136135e68d54af6d9b0cfbb8", "sha256": "59fd9d6ca09ade4f27bddd274fb842ea48fd92118a755d0a64cf60413cd1c3fc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4-sources.jar"} , "name": "assembly_commons_cli_commons_cli", "actual": "@assembly_commons_cli_commons_cli//jar", "bind": "jar/assembly_commons_cli/commons_cli"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
