import java.io.Serializable

class ScanBean(
    var owner: String = "",
    var name: String = "",
    var desc: String = "",
    var replaceOpcode: Int = 0,
    var replaceOwner: String = "",
    var replaceName: String = "",
    var replaceDesc: String = "",
) : Cloneable, Serializable {

    public override fun clone(): ScanBean {
        return try {
            super.clone() as ScanBean
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            ScanBean()
        }
    }

}

//作者：miaowmiaow
//链接：https://juejin.cn/post/7121985493445083149
//来源：稀土掘金
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。