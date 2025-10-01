Pod::Spec.new do |spec|
    spec.name                     = 'ClipboardManager'
    spec.version                  = '1.0'
    spec.homepage                 = 'https://github.com/niyajali/clipboard-manager'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Sk Niyaj Ali'
    spec.license                  = 'Apache License 2.0'
    spec.summary                  = 'A Kotlin Multiplatform library for monitoring system clipboard changes across all major platforms including Android, JVM Desktop, iOS, JavaScript, and WebAssembly with support for text, HTML, RTF, files, and images.'
    spec.vendored_frameworks      = 'build/cocoapods/framework/ClipboardManager.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '13.5'
                
                
    if !Dir.exist?('build/cocoapods/framework/ClipboardManager.framework') || Dir.empty?('build/cocoapods/framework/ClipboardManager.framework')
        raise "

        Kotlin framework 'ClipboardManager' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => '',
        'PRODUCT_MODULE_NAME' => 'ClipboardManager',
    }
                
    spec.script_phases = [
        {
            :name => 'Build ClipboardManager',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end