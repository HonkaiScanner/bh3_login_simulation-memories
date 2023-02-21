/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

abstract class ASMPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                EMyClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {
                it.ignoreOwner.set("com/github/haocen2004/login_simulation/utils/ASMHelper")
                it.listOfScans.set(
                    listOf(
                        ScanBean(
                            "LDC",
                            "LDC",
                            "com.xiaomi.gamecenter.sdk.service.ban",
                            Opcodes.INVOKESTATIC,
                            "com/github/haocen2004/login_simulation/utils/ASMHelper",
                            "getMiPackageName",
                            "()Ljava/lang/String;"
                        ),
                        ScanBean(
                            "LDC",
                            "LDC",
                            "com.xiaomi.gamecenter.sdk.service",
                            Opcodes.INVOKESTATIC,
                            "com/github/haocen2004/login_simulation/utils/ASMHelper",
                            "getMiPackageName",
                            "()Ljava/lang/String;"
                        )
                    )
                )


            }
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }

    interface ExampleParams : InstrumentationParameters {
        @get:Input
        val ignoreOwner: Property<String>

        @get:Input
        val listOfScans: ListProperty<ScanBean>
    }

    abstract class EMyClassVisitorFactory :
        AsmClassVisitorFactory<ExampleParams> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor {
            return ScanClassNode(
                nextClassVisitor,
                parameters.get().listOfScans.get(),
            )
        }

        override fun isInstrumentable(classData: ClassData): Boolean {
            return !classData.className.startsWith(
                parameters.get().ignoreOwner.get().replace("/", ".")
            )
        }

    }
}