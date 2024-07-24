package teamcity

import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class BuildDetails private constructor(
    val buildUrl: String,
    val buildName: String
) {

    companion object {
        fun parse(xml: String): BuildDetails {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = builder.parse(InputSource(StringReader(xml)))
            val buildTag = document.getElementsByTagName("build")
            var buildUrl = ""
            var buildName = ""
            if (buildTag.length > 0) {
                buildUrl = buildTag.item(0).getAttributeValue("webUrl")
            }
            val buildTypeTag = document.getElementsByTagName("buildType")
            if (buildTypeTag.length > 0) {
                buildName = buildTypeTag.item(0).getAttributeValue("name")
            }
            return BuildDetails(buildUrl, buildName)
        }
    }

}

fun Node.getAttributeValue(key: String): String {
    for (i in 0 until attributes.length) {
        val attribute = attributes.item(i)
        if (attribute.nodeName == key) {
            return attribute.textContent
        }
    }
    return ""
}