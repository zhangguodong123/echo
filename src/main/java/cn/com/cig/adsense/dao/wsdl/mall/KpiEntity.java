
package cn.com.cig.adsense.dao.wsdl.mall;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>KpiEntity complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="KpiEntity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CityId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="CsId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Adlink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MallPrice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Slogan" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="showcarid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="showyear" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CarName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ImageUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KpiEntity", propOrder = {
    "cityId",
    "csId",
    "adlink",
    "mallPrice",
    "slogan",
    "showcarid",
    "showyear",
    "carName",
    "imageUrl"
})
public class KpiEntity {

    @XmlElement(name = "CityId")
    protected int cityId;
    @XmlElement(name = "CsId")
    protected int csId;
    @XmlElement(name = "Adlink")
    protected String adlink;
    @XmlElement(name = "MallPrice")
    protected String mallPrice;
    @XmlElement(name = "Slogan")
    protected String slogan;
    protected String showcarid;
    protected String showyear;
    @XmlElement(name = "CarName")
    protected String carName;
    @XmlElement(name = "ImageUrl")
    protected String imageUrl;

    /**
     * 获取cityId属性的值。
     * 
     */
    public int getCityId() {
        return cityId;
    }

    /**
     * 设置cityId属性的值。
     * 
     */
    public void setCityId(int value) {
        this.cityId = value;
    }

    /**
     * 获取csId属性的值。
     * 
     */
    public int getCsId() {
        return csId;
    }

    /**
     * 设置csId属性的值。
     * 
     */
    public void setCsId(int value) {
        this.csId = value;
    }

    /**
     * 获取adlink属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdlink() {
        return adlink;
    }

    /**
     * 设置adlink属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdlink(String value) {
        this.adlink = value;
    }

    /**
     * 获取mallPrice属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMallPrice() {
        return mallPrice;
    }

    /**
     * 设置mallPrice属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMallPrice(String value) {
        this.mallPrice = value;
    }

    /**
     * 获取slogan属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSlogan() {
        return slogan;
    }

    /**
     * 设置slogan属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSlogan(String value) {
        this.slogan = value;
    }

    /**
     * 获取showcarid属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowcarid() {
        return showcarid;
    }

    /**
     * 设置showcarid属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowcarid(String value) {
        this.showcarid = value;
    }

    /**
     * 获取showyear属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowyear() {
        return showyear;
    }

    /**
     * 设置showyear属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowyear(String value) {
        this.showyear = value;
    }

    /**
     * 获取carName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCarName() {
        return carName;
    }

    /**
     * 设置carName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarName(String value) {
        this.carName = value;
    }

    /**
     * 获取imageUrl属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * 设置imageUrl属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageUrl(String value) {
        this.imageUrl = value;
    }

}
