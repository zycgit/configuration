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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
/**
 *
 * @version 2010-9-8
 * @author 赵永春 (zyc@hasor.net)
 */
public class TextEvent extends XmlStreamEvent {
    private Type type = null;
    public TextEvent(final String xpath, final XMLStreamReader reader, final Type type) {
        super(xpath, reader);
        this.type = type;
    }
    /**
     *
     * @version 2010-9-11
     * @author 赵永春 (zyc@hasor.net)
     */
    public enum Type {
        /***/
        CDATA, /***/
        Chars, /***/
        Comment, /***/
        Space,
    }
    /**如果当前事件是一个CDATA事件则返回true。*/
    public boolean isCDATAEvent() {
        return this.type == Type.CDATA;
    }
    ;
    /**如果当前事件是一个Chars事件则返回true。*/
    public boolean isCharsEvent() {
        return this.type == Type.Chars;
    }
    ;
    /**如果当前事件是一个Space事件则返回true。*/
    public boolean isSpaceEvent() {
        return this.type == Type.Space;
    }
    ;
    /**如果当前事件是一个Comment事件则返回true。*/
    public boolean isCommentEvent() {
        return this.type == Type.Comment;
    }
    ;
    /** 如果光标指向由所有空格组成的字符数据事件，则返回 true。*/
    public boolean isWhiteSpace() {
        return this.getReader().isWhiteSpace();
    }
    /**以字符串的形式返回去掉前后空格和回车的getText()值。 */
    public String getTrimText() {
        String value = this.getText();
        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }
    private String textData = null;
    /**以字符串的形式返回解析事件的当前值，此方法返回 CHARACTERS 事件的字符串值，返回 COMMENT 的值、CDATA 节的字符串值、SPACE 事件的字符串值。 */
    public String getText() {
        if (this.textData == null) {
            char[] data = new char[this.getReader().getTextLength()];
            System.arraycopy(this.getReader().getTextCharacters(), this.getReader().getTextStart(), data, 0, this.getReader().getTextLength());
            this.textData = new String(data);
        }
        return this.textData;
    }
    ;
    public String getOriginalText() {
        String originalText = this.getText();
        if (originalText != null) {
            originalText = originalText.replace("&", "&amp;");//& 和
            originalText = originalText.replace("<", "&lt;");//小于号
            originalText = originalText.replace(">", "&gt;");//大于号
            originalText = originalText.replace("'", "&apos;");//'单引号
            originalText = originalText.replace("\"", "&quot;");//'双引号
        }
        return originalText;
    }
    ;
    /**返回一个包含此事件中字符的数组。 */
    public char[] getTextCharacters() {
        return this.getReader().getTextCharacters();
    }
    ;
    /**获取与 CHARACTERS、SPACE 或 CDATA 事件关联的文本。 */
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
        return this.getReader().getTextCharacters(sourceStart, target, targetStart, length);
    }
    ;
    /**返回文本字符数组中此文本事件的字符序列长度。*/
    public int getTextLength() {
        return this.getReader().getTextLength();
    }
    ;
    /** 返回存储（此文本事件的）第一个字符位置处的文本字符数组的偏移量。 */
    public int getTextStart() {
        return this.getReader().getTextStart();
    }
    /**该事件的拍档是它自己。*/
    @Override
    public boolean isPartner(final XmlStreamEvent e) {
        if (e instanceof TextEvent) {
            return true;
        } else {
            return false;
        }
    }
    ;
    /**文本事件，如果文本类型是Comment，则是共有事件。其他为私有事件。*/
    @Override
    public boolean isPublicEvent() {
        if (this.isCommentEvent() == true) {
            return true;
        } else {
            return false;
        }
    }
}