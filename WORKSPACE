load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
git_repository(
    name = "com_google_protobuf",
    remote = "https://github.com/google/protobuf.git",
    tag = "v3.6.1.3"
)

load("//3rdparty:workspace.bzl", "maven_dependencies")
maven_dependencies()
