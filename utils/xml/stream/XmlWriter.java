/*
 * Copyright 2008-2009 the original author or authors.
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
package org.more.xml.stream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
/**
 *
 * @version 2010-9-7
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlWriter implements XmlAccept {
    private OutputStream    xmlStrema     = null; //读取Xml数据的输出流。
    private XMLStreamWriter writer        = null;
    private boolean         ignoreComment = true; //是否忽略Xml中的所有注释节点。
    private boolean         ignoreSpace   = true; //是否忽略Xml中可忽略的空格。
    //--------------------------------------------------------------------
    /**创建一个XmlWriter对象用于写入xml事件流到fileName参数所表述的Xml文件。*/
    public XmlWriter(final String fileName) throws FileNotFoundException {
        this.xmlStrema = new FileOutputStream(fileName);
    }
    /**创建一个XmlWriter对象用于写入xml事件流到file参数所表述的Xml文件。*/
    public XmlWriter(final File file) throws FileNotFoundException {
        this.xmlStrema = new FileOutputStream(file);
    }
    /**创建一个XmlWriter对象用于写入xml事件流到xmlStrema参数所表述的流中。*/
    public XmlWriter(final OutputStream xmlStrema) {
        if (xmlStrema == null) {
            throw new NullPointerException("OutputStream类型参数为空。");
        }
        this.xmlStrema = xmlStrema;
    }
    //--------------------------------------------------------------------
    /**返回一个boolean值，该值表示了是否忽略在写入XML期间发现的描述节点。返回true表示忽略，false表示不忽略。*/
    public boolean isIgnoreComment() {
        return this.ignoreComment;
    }
    /**设置一个boolean值，该值表示了是否忽略在写入XML期间发现的描述节点。true表示忽略，false表示不忽略。*/
    public void setIgnoreComment(final boolean ignoreComment) {
        this.ignoreComment = ignoreComment;
    }
    /**返回一个boolean值，该值表示了是否忽略在读取XML期间发现的可忽略的空格字符（参阅 [XML], 2.10 "White Space Handling"）。返回true表示忽略，false表示不忽略。*/
    public boolean isIgnoreSpace() {
        return this.ignoreSpace;
    }
    /**设置一个boolean值，该值表示了是否在写入XML期间忽略可忽略的空格字符（参阅 [XML], 2.10 "White Space Handling"）。true表示忽略，false表示不忽略。*/
    public void setIgnoreSpace(final boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }
    //--------------------------------------------------------------------
    @Override
    public void beginAccept() throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        this.writer = factory.createXMLStreamWriter(this.xmlStrema);
    }
    @Override
    public void endAccept() {
    }
    @Override
    public void sendEvent(final XmlStreamEvent e) throws XMLStreamException {
        //1.执行忽略。
        if (e instanceof TextEvent == true) {
            TextEvent textE = (TextEvent) e;
            if (textE.isCommentEvent() == true && this.ignoreComment == true) {
                return;
            }
            if (textE.isSpaceEvent() == true && this.ignoreSpace == true) {
                return;
            }
        }
        //2.处理事件
        if (e instanceof StartDocumentEvent) {
            StartDocumentEvent ee = (StartDocumentEvent) e;
            this.writer.writeStartDocument(ee.getEncoding(), ee.getVersion());
        } else if (e instanceof EndDocumentEvent) {
            this.writer.writeEndDocument();
        } else if (e instanceof StartElementEvent) {
            StartElementEvent ee = (StartElementEvent) e;
            this.writer.writeStartElement(ee.getPrefix(), ee.getElementName(), ee.getNamespaceURI());
        } else if (e instanceof EndElementEvent) {
            this.writer.writeEndElement();
        } else if (e instanceof AttributeEvent) {
            AttributeEvent ee = (AttributeEvent) e;
            this.writer.writeAttribute(ee.getPrefix(), ee.getNamespaceURI(), ee.getElementName(), ee.getValue());
        } else if (e instanceof TextEvent) {
            TextEvent ee = (TextEvent) e;
            if (ee.isCommentEvent() == true) {
                this.writer.writeComment(ee.getText());
            }
            if (ee.isCDATAEvent() == true) {
                this.writer.writeCData(ee.getText());
            }
            if (ee.isCharsEvent() == true) {
                this.writer.writeCharacters(ee.getText());
                //if (ee.isSpaceEvent() == true  )
                //if (ee.isWhiteSpace() == true)
            }
        }
        //end writer
    }
}