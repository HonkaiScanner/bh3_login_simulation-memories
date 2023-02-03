import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ScanClassNode(
    private val classVisitor: ClassVisitor,
    private val scans: List<ScanBean>, //配置的对象（包含目标信息和替换信息）
) : ClassNode(Opcodes.ASM9) { //ASM Tree API 会把 class 文件包装成 ClassNode 方便我们操作

    override fun visitEnd() { //顾名思义访问完成的回调，我们在这里可以获取 class 文件的所有字段和方法

        //遍历所有方法
        methods.forEach { methodNode ->
            val instructions = methodNode.instructions
            //遍历方法内的每一行代码
            val iterator = instructions.iterator()
            while (iterator.hasNext()) {
                val insnNode = iterator.next()
                //ASM Tree API  会把字段包装成 FieldInsnNode ，方法包装成 MethodInsnNode
                //查找目标字段或方法
                if (insnNode is FieldInsnNode) {
                    //以Build.BRAND举例，对应的 owner = "android/os/Build"，name = "BRAND"，desc = "Ljava/lang/String;"
                    scans.find {
                        it.owner == insnNode.owner && it.name == insnNode.name && it.desc == insnNode.desc
                    }?.let {
                        //通过 instructions.set 替换目标字段
                        instructions.set(insnNode, newInsnNode(it))
                    }
                }
                if (insnNode is MethodInsnNode) {
                    //以OnClickListener.onClick(View v)举例，对应的 owner = "Landroid/view/View$OnClickListener;"，name = "onClick"，desc = "(Landroid/view/View;)V"
                    scans.find {
                        it.owner == insnNode.owner && it.name == insnNode.name && it.desc == insnNode.desc
                    }?.let {
                        //通过 instructions.set 替换目标方法
                        instructions.set(insnNode, newInsnNode(it))
                    }
                }
                if (insnNode is LdcInsnNode) {
                    scans.find {
                        insnNode.cst.toString() == it.desc
                    }?.let {
                        instructions.set(insnNode, newInsnNode(it))
                    }
                }
            }
        }
        super.visitEnd()
        //将 ClassNode 类中字段的值传递给下一个 ClassVisitor 类实例
        accept(classVisitor)
    }

    //构建替换的字段或方法
    private fun newInsnNode(bean: ScanBean): AbstractInsnNode {
        val opcode = bean.replaceOpcode
        val owner = bean.replaceOwner
        val name = bean.replaceName
        val descriptor = bean.replaceDesc
        return if (!bean.replaceDesc.startsWith("(")) { //根据"("判断字段或方法
            FieldInsnNode(opcode, owner, name, descriptor)
        } else {
            MethodInsnNode(opcode, owner, name, descriptor, false)
        }
    }
}