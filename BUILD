
proto_library(
    name = "deps_proto",
    srcs = ["deps.proto"]
)

java_proto_library(
    name = "deps_java_proto",
    deps = [":deps_proto"],
)

java_binary(
    name = "assembly",
    srcs = glob(["src/main/java/com/z/bazel_assembly/*.java"]),
    main_class = "com.z.bazel_assembly.Main",
    deps = [
        ":deps_java_proto",
        "//3rdparty/commons_cli:commons_cli",
    ],
    visibility = ["//visibility:public"],
)

# TODO: write an example with transtive dep instead of using the CLI
load("//:assembly.bzl", "java_assembly_jar")
java_assembly_jar(
    name = "test",
    main_class = "com.z.bazel_assembly.Main",
    srcs = glob(["src/main/java/com/z/bazel_assembly/*.java"]),
    deps = [
        ":deps_java_proto",
        "//3rdparty/commons_cli:commons_cli",
    ]
)
