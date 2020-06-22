# See this file for helpful comments explaining PyOxidizer:
# https://github.com/indygreg/PyOxidizer/blob/143785b54a87554f4398071d820bf2788d9540b7/pyoxidizer/src/templates/new-pyoxidizer.bzl

def make_dist():
    return default_python_distribution()

def make_exe(dist):
    python_config = PythonInterpreterConfig(
         run_module="index",
    )

    exe = dist.to_python_executable(
        name="pyoxidizer",
        config=python_config,
        extension_module_filter='all',
        include_sources=True,
        include_resources=False,
        include_test=False,
    )

    exe.add_in_memory_python_resources(dist.pip_install(["-r", "requirements.txt"]))

    exe.add_python_resources(dist.read_package_root(
        path=".",
        packages=["index"],
    ))

    return exe

def make_embedded_data(exe):
    return exe.to_embedded_data()

def make_install(exe):
    files = FileManifest()
    files.add_python_resource(".", exe)
    return files

register_target("dist", make_dist)
register_target("exe", make_exe, depends=["dist"], default=True)
register_target("embedded", make_embedded_data, depends=["exe"], default_build_script=True)
register_target("install", make_install, depends=["exe"])

resolve_targets()

# END OF COMMON USER-ADJUSTED SETTINGS.
#
# Everything below this is typically managed by PyOxidizer and doesn't need
# to be updated by people.

PYOXIDIZER_VERSION = "0.6.0"
PYOXIDIZER_COMMIT = "UNKNOWN"
