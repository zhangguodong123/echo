
package cn.com.cig.adsense.dao.wsdl.yipai;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "YjkForAdvertizementSoap", targetNamespace = "http://tempuri.org/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface YjkForAdvertizementSoap {


    /**
     * 获取精准广告集客单记录信息
     * 
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "GetYjkBasicForAdvertizement", action = "http://tempuri.org/GetYjkBasicForAdvertizement")
    @WebResult(name = "GetYjkBasicForAdvertizementResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetYjkBasicForAdvertizement", targetNamespace = "http://tempuri.org/", className = "cn.com.cig.adsense.externalinterface.yipai.GetYjkBasicForAdvertizement")
    @ResponseWrapper(localName = "GetYjkBasicForAdvertizementResponse", targetNamespace = "http://tempuri.org/", className = "cn.com.cig.adsense.externalinterface.yipai.GetYjkBasicForAdvertizementResponse")
    public String getYjkBasicForAdvertizement();

}
