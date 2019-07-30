def _assembly_jar_impl(ctx):
    inputs = []
    args = []
    jar = ctx.attr.jar

    for dep in ctx.attr.deps:
        for file in dep[JavaInfo].full_compile_jars:
            inputs.append(file)
            args.extend(["--include", file.path])

    for file in jar[JavaInfo].transitive_runtime_deps:
        if file in jar[OutputGroupInfo].compilation_outputs:
            continue
        inputs.append(file)
        args.extend(["--deps", file.path])

    args.extend(["--output", ctx.outputs.jar.path])
    args.extend(["--jar", ctx.file.jar.path])
    inputs.append(ctx.file.jar)
    inputs.append(jar[JavaInfo].outputs.jdeps)

    if ctx.attr.main_class != "":
        args.extend(["--main_class", ctx.attr.main_class])

    ctx.action(
        inputs = inputs,
        outputs = [ctx.outputs.jar],
        executable = ctx.executable._assembly_jar,
        arguments = args,
    )

assembly_jar = rule(
    _assembly_jar_impl,
    attrs = {
        "jar": attr.label(
            allow_single_file = [".jar"],
            providers = [JavaInfo, OutputGroupInfo],
            mandatory = True,
        ),
        "_assembly_jar": attr.label(
            default = Label("//:assembly"),
            allow_files = True,
            cfg = "host",
            executable = True,
        ),
        "deps": attr.label_list(
            providers = [JavaInfo, OutputGroupInfo],
        ),
        "main_class": attr.string(),
    },
    outputs = {
        "jar": "%{name}.jar",
    },
)

def java_assembly_jar(name, deps = [], main_class = None, **kwargs):
    native.java_library(
        name = name,
        deps = deps,
        **kwargs
    )
    assembly_jar(
        name = name + "_assembly",
        jar = ":" + name,
        deps = deps,
        main_class = main_class,
    )
