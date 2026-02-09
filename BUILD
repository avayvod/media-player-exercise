load("//tools/build_defs/android:rules.bzl", "android_binary", "android_library")

android_library(
    name = "app_lib",
    srcs = glob(["*.kt"]),
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//java/com/google/android/libraries/material/compose:google_material_3_theme",
        "//third_party/java/androidx/activity",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material/icons_extended",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core",
        "//third_party/java/androidx/lifecycle/runtime_ktx",
        "//third_party/java/androidx/lifecycle/viewmodel",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_binary(
    name = "app",
    manifest = "AndroidManifest.xml",
    deps = [":app_lib"],
)
