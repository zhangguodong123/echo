
package cn.com.cig.adsense.dao.wsdl.mall;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Items" type="{http://www.yichemall.com/}ReturnKpiEntity"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "items"
})
@XmlRootElement(name = "GetKpiCarInfoResponse")
public class GetKpiCarInfoResponse {

    @XmlElement(name = "Items", required = true, nillable = true)
    protected ReturnKpiEntity items;

    /**
     * 获取items属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ReturnKpiEntity }
     *     
     */
    public ReturnKpiEntity getItems() {
        return items;
    }

    /**
     * 设置items属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnKpiEntity }
     *     
     */
    public void setItems(ReturnKpiEntity value) {
        this.items = value;
    }

}
