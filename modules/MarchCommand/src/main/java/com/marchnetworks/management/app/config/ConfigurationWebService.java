package com.marchnetworks.management.app.config;

import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.config.service.ConfigView;
import com.marchnetworks.management.config.service.ConfigurationException;
import com.marchnetworks.management.config.service.DeviceConfigDescriptor;
import com.marchnetworks.management.config.service.DeviceImageDescriptor;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.springframework.security.access.AccessDeniedException;



@WebService(serviceName="ConfigurationService", name="ConfigurationService", portName="ConfigurationPort")
public class ConfigurationWebService
{
  private ConfigService configService = (ConfigService)ApplicationContextSupport.getBean("configServiceProxy");
  private String accessDenied = "not_authorized";
  









  @WebMethod(operationName="importImage")
  public DeviceImageDescriptor importImage(@WebParam(name="deviceId") String devId, @WebParam(name="name") String name, @WebParam(name="description") String description)
    throws ConfigurationException
  {
    try
    {
      return configService.createImage(devId, name, description);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  




  @WebMethod(operationName="listAllImages")
  public DeviceImageDescriptor[] listAllImages()
    throws ConfigurationException
  {
    try
    {
      return configService.listImages();
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  



  @WebMethod(operationName="getImage")
  public DeviceImageDescriptor getImage(@WebParam(name="imageId") String imageId)
    throws ConfigurationException
  {
    try
    {
      return configService.getImage(imageId);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  










  @WebMethod(operationName="editImage")
  public DeviceImageDescriptor editImage(@WebParam(name="ImageId") String imageId, @WebParam(name="name") String name, @WebParam(name="description") String description)
    throws ConfigurationException
  {
    try
    {
      return configService.editImage(imageId, name, description);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  




  @WebMethod(operationName="deleteImage")
  public DeviceImageDescriptor deleteImage(@WebParam(name="imageId") String imageId)
    throws ConfigurationException
  {
    try
    {
      return configService.deleteImage(imageId);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  


  @WebMethod(operationName="applyImages")
  public void applyImages(@WebParam(name="configurations") ConfigView[] configs)
    throws ConfigurationException
  {
    try
    {
      configService.applyImages(configs);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  



  @WebMethod(operationName="getDeviceImageStatus")
  public DeviceImageState getDeviceImageStatus(@WebParam(name="deviceId") String deviceId)
    throws ConfigurationException
  {
    try
    {
      return configService.getDeviceImageStatus(deviceId);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  



  @WebMethod(operationName="getAllDeviceConfig")
  public DeviceConfigDescriptor[] getAllDeviceConfig()
    throws ConfigurationException
  {
    try
    {
      return configService.getAllDeviceConfig();
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  



  @WebMethod(operationName="getDeviceConfig")
  public DeviceConfigDescriptor getDeviceConfig(@WebParam(name="deviceConfigId") String deviceConfigId)
    throws ConfigurationException
  {
    try
    {
      return configService.getDeviceConfig(deviceConfigId);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  
  @WebMethod(operationName="getConfigurationSettings")
  public ConfigSettings getConfigurationSettings() throws ConfigurationException {
    try {
      return configService.getConfigurationSettings();
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
  
  @WebMethod(operationName="setConfigurationSettings")
  public void setConfigurationSettings(@WebParam(name="settings") ConfigSettings settings) throws ConfigurationException {
    try {
      configService.setConfigurationSettings(settings);
    } catch (AccessDeniedException e) {
      throw new ConfigurationException(accessDenied);
    }
  }
}
