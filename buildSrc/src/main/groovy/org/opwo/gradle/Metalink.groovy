package org.opwo.gradle

import groovy.util.*
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.text.SimpleDateFormat
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import org.apache.tools.ant.types.FileSet

class Metalink extends DefaultTask {

    String fileSet
    String url
    String outputFile
    Document xml

    @TaskAction
    def generateLinks() {
      //Get url from properties if not defined
      if (url == null) {
          url = project.properties["serverFilesUrl"]
      }

      //document root
      xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
  		def xmlns = xml.createElementNS("urn:ietf:params:xml:ns:metalink","metalink")
  		xml.appendChild(xmlns)

      //published date
  		SimpleDateFormat dateFormat
  		dateFormat = new SimpleDateFormat('dd-MMM-yyyy')
  		def publishedElement = xml.createElement("published")
  		publishedElement.appendChild(xml.createTextNode(dateFormat.format(new Date())))
  		xmlns.appendChild(publishedElement)

      //file list
  		new File(fileSet).eachFileRecurse(FileType.FILES) { file ->
          def fileElement = xml.createElement("file")
      		fileElement.setAttribute("name",file.name)

      		def sizeElement = xml.createElement("size")
      		sizeElement.appendChild(xml.createTextNode(Long.toString(file.length())))
          fileElement.appendChild(sizeElement)

      		def md5Element = xml.createElement("hash")
      		md5Element.appendChild(xml.createTextNode(new MDP().getMDP(file)))
      		md5Element.setAttribute("type","md5")
          fileElement.appendChild(md5Element)

      		def urlElement = xml.createElement("url")
          def path = file.absolutePath
          path = path.replace("\\", "/")
          path = path.replace(fileSet, "")
      		urlElement.appendChild(xml.createTextNode(url + path))
      		fileElement.appendChild(urlElement)

  				xmlns.appendChild(fileElement)
  		}

      //write the content into xml file
  		def transformerFactory = TransformerFactory.newInstance()
  		def transformer = transformerFactory.newTransformer()
  		def source = new DOMSource(xml)
  		def result = new StreamResult(new FileOutputStream(outputFile))
  		transformer.transform(source, result)
    }
}
