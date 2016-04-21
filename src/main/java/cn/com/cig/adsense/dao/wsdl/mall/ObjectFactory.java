
package cn.com.cig.adsense.dao.wsdl.mall;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cn.com.cig.adsense.externalinterface.yichemall package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cn.com.cig.adsense.externalinterface.yichemall
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetAllCarInfoList }
     * 
     */
    public GetAllCarInfoList createGetAllCarInfoList() {
        return new GetAllCarInfoList();
    }

    /**
     * Create an instance of {@link GetKpiCarInfo }
     * 
     */
    public GetKpiCarInfo createGetKpiCarInfo() {
        return new GetKpiCarInfo();
    }

    /**
     * Create an instance of {@link GetKpiCarInfoResponse }
     * 
     */
    public GetKpiCarInfoResponse createGetKpiCarInfoResponse() {
        return new GetKpiCarInfoResponse();
    }

    /**
     * Create an instance of {@link ReturnKpiEntity }
     * 
     */
    public ReturnKpiEntity createReturnKpiEntity() {
        return new ReturnKpiEntity();
    }

    /**
     * Create an instance of {@link GetAllCarInfoListResponse }
     * 
     */
    public GetAllCarInfoListResponse createGetAllCarInfoListResponse() {
        return new GetAllCarInfoListResponse();
    }

    /**
     * Create an instance of {@link KpiEntity }
     * 
     */
    public KpiEntity createKpiEntity() {
        return new KpiEntity();
    }

}
