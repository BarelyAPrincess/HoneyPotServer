package com.marchnetworks.device_ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService( name = "DeviceServiceSoap", targetNamespace = "http://marchnetworks.com/device_ws/" )
@XmlSeeAlso( {ObjectFactory.class} )
public abstract interface DeviceServiceSoap
{
	@WebMethod( operationName = "GetServiceCapabilities", action = "http://marchnetworks.com/device_ws/GetServiceCapabilities" )
	@WebResult( name = "GetServiceCapabilitiesResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetServiceCapabilities", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetServiceCapabilities" )
	@ResponseWrapper( localName = "GetServiceCapabilitiesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetServiceCapabilitiesResponse" )
	public abstract Capabilities getServiceCapabilities();

	@WebMethod( operationName = "GetTime", action = "http://marchnetworks.com/device_ws/GetTime" )
	@WebResult( name = "GetTimeResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetTime", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetTime" )
	@ResponseWrapper( localName = "GetTimeResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetTimeResponse" )
	public abstract Timestamp getTime();

	@WebMethod( operationName = "CancelRequests", action = "http://marchnetworks.com/device_ws/CancelRequests" )
	@RequestWrapper( localName = "CancelRequests", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CancelRequests" )
	@ResponseWrapper( localName = "CancelRequestsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CancelRequestsResponse" )
	public abstract void cancelRequests( @WebParam( name = "requestIds", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "GetSystemDetails", action = "http://marchnetworks.com/device_ws/GetSystemDetails" )
	@WebResult( name = "GetSystemDetailsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetSystemDetails", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetSystemDetails" )
	@ResponseWrapper( localName = "GetSystemDetailsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetSystemDetailsResponse" )
	public abstract DeviceDetails getSystemDetails();

	@WebMethod( operationName = "GetRegistrationDetails", action = "http://marchnetworks.com/device_ws/GetRegistrationDetails" )
	@WebResult( name = "GetRegistrationDetailsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetRegistrationDetails", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetRegistrationDetails" )
	@ResponseWrapper( localName = "GetRegistrationDetailsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetRegistrationDetailsResponse" )
	public abstract RegistrationDetails getRegistrationDetails();

	@WebMethod( operationName = "UpdateRegistrationDetails", action = "http://marchnetworks.com/device_ws/UpdateRegistrationDetails" )
	@RequestWrapper( localName = "UpdateRegistrationDetails", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.UpdateRegistrationDetails" )
	@ResponseWrapper( localName = "UpdateRegistrationDetailsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.UpdateRegistrationDetailsResponse" )
	public abstract void updateRegistrationDetails( @WebParam( name = "serverAddresses", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "Register", action = "http://marchnetworks.com/device_ws/Register" )
	@WebResult( name = "RegisterResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "Register", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.Register" )
	@ResponseWrapper( localName = "RegisterResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.RegisterResponse" )
	public abstract String register( @WebParam( name = "serverAddress", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString1, @WebParam( name = "serverPath", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString1, @WebParam( name = "deviceId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString2, @WebParam( name = "serverHostname", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString2, @WebParam( name = "serverAddresses", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString3 );

	@WebMethod( operationName = "Unregister", action = "http://marchnetworks.com/device_ws/Unregister" )
	@RequestWrapper( localName = "Unregister", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.Unregister" )
	@ResponseWrapper( localName = "UnregisterResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.UnregisterResponse" )
	public abstract void unregister();

	@WebMethod( operationName = "SetCertificateChain", action = "http://marchnetworks.com/device_ws/SetCertificateChain" )
	@RequestWrapper( localName = "SetCertificateChain", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetCertificateChain" )
	@ResponseWrapper( localName = "SetCertificateChainResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetCertificateChainResponse" )
	public abstract void setCertificateChain( @WebParam( name = "serverCertificatesForDevice", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "Subscribe", action = "http://marchnetworks.com/device_ws/Subscribe" )
	@WebResult( name = "SubscribeResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "Subscribe", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.Subscribe" )
	@ResponseWrapper( localName = "SubscribeResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SubscribeResponse" )
	public abstract String subscribe( @WebParam( name = "eventPrefixes", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString, @WebParam( name = "timeoutSeconds", targetNamespace = "http://marchnetworks.com/device_ws/" ) double paramDouble, @WebParam( name = "offlineSeqNo", targetNamespace = "http://marchnetworks.com/device_ws/" ) Long paramLong );

	@WebMethod( operationName = "SubscribeEventsNotify", action = "http://marchnetworks.com/device_ws/SubscribeEventsNotify" )
	@WebResult( name = "SubscribeResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "SubscribeEventsNotify", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SubscribeEventsNotify" )
	@ResponseWrapper( localName = "SubscribeEventsNotifyResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SubscribeEventsNotifyResponse" )
	public abstract String subscribeEventsNotify( @WebParam( name = "eventPrefixes", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString, @WebParam( name = "timeoutSeconds", targetNamespace = "http://marchnetworks.com/device_ws/" ) double paramDouble1, @WebParam( name = "notifyContextPath", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString, @WebParam( name = "maxNotifyTime", targetNamespace = "http://marchnetworks.com/device_ws/" ) double paramDouble2, @WebParam( name = "offlineSeqNo", targetNamespace = "http://marchnetworks.com/device_ws/" ) Long paramLong );

	@WebMethod( operationName = "Unsubscribe", action = "http://marchnetworks.com/device_ws/Unsubscribe" )
	@RequestWrapper( localName = "Unsubscribe", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.Unsubscribe" )
	@ResponseWrapper( localName = "UnsubscribeResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.UnsubscribeResponse" )
	public abstract void unsubscribe( @WebParam( name = "subscriptionId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString );

	@WebMethod( operationName = "ModifySubscription", action = "http://marchnetworks.com/device_ws/ModifySubscription" )
	@RequestWrapper( localName = "ModifySubscription", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ModifySubscription" )
	@ResponseWrapper( localName = "ModifySubscriptionResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ModifySubscriptionResponse" )
	public abstract void modifySubscription( @WebParam( name = "subscriptionId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString, @WebParam( name = "eventPrefixes", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "GetWaitingEvents", action = "http://marchnetworks.com/device_ws/GetWaitingEvents" )
	@WebResult( name = "GetWaitingEventsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetWaitingEvents", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetWaitingEvents" )
	@ResponseWrapper( localName = "GetWaitingEventsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetWaitingEventsResponse" )
	public abstract ArrayOfEvent getWaitingEvents( @WebParam( name = "subscriptionId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString, @WebParam( name = "timeoutSeconds", targetNamespace = "http://marchnetworks.com/device_ws/" ) double paramDouble );

	@WebMethod( operationName = "GetParameters", action = "http://marchnetworks.com/device_ws/GetParameters" )
	@WebResult( name = "GetParametersResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetParameters", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetParameters" )
	@ResponseWrapper( localName = "GetParametersResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetParametersResponse" )
	public abstract GetParametersResult getParameters( @WebParam( name = "paramPrefixes", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "SetParameters", action = "http://marchnetworks.com/device_ws/SetParameters" )
	@RequestWrapper( localName = "SetParameters", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetParameters" )
	@ResponseWrapper( localName = "SetParametersResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetParametersResponse" )
	public abstract void setParameters( @WebParam( name = "parameters", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfGenericParameter paramArrayOfGenericParameter );

	@WebMethod( operationName = "GetConfigHash", action = "http://marchnetworks.com/device_ws/GetConfigHash" )
	@WebResult( name = "GetConfigHashResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetConfigHash", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetConfigHash" )
	@ResponseWrapper( localName = "GetConfigHashResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetConfigHashResponse" )
	public abstract String getConfigHash();

	@WebMethod( operationName = "GetChannelConfigHashes", action = "http://marchnetworks.com/device_ws/GetChannelConfigHashes" )
	@WebResult( name = "GetChannelConfigHashResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetChannelConfigHashes", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetChannelConfigHashes" )
	@ResponseWrapper( localName = "GetChannelConfigHashesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetChannelConfigHashesResponse" )
	public abstract ArrayOfHashResult getChannelConfigHashes( @WebParam( name = "channelIds", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "GetAllChannelDetails", action = "http://marchnetworks.com/device_ws/GetAllChannelDetails" )
	@WebResult( name = "GetAllChannelDetailsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAllChannelDetails", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAllChannelDetails" )
	@ResponseWrapper( localName = "GetAllChannelDetailsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAllChannelDetailsResponse" )
	public abstract ArrayOfChannelDetails getAllChannelDetails();

	@WebMethod( operationName = "GetAudioOutputs", action = "http://marchnetworks.com/device_ws/GetAudioOutputs" )
	@WebResult( name = "GetAudioOutputsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAudioOutputs", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAudioOutputs" )
	@ResponseWrapper( localName = "GetAudioOutputsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAudioOutputsResponse" )
	public abstract ArrayOfAudioOutput getAudioOutputs();

	@WebMethod( operationName = "GetChannelDetails", action = "http://marchnetworks.com/device_ws/GetChannelDetails" )
	@WebResult( name = "GetChannelDetailsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetChannelDetails", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetChannelDetails" )
	@ResponseWrapper( localName = "GetChannelDetailsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetChannelDetailsResponse" )
	public abstract ChannelDetails getChannelDetails( @WebParam( name = "id", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString );

	@WebMethod( operationName = "ExecuteRawCommand", action = "http://marchnetworks.com/device_ws/ExecuteRawCommand" )
	@WebResult( name = "ExecuteRawCommandResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "ExecuteRawCommand", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ExecuteRawCommand" )
	@ResponseWrapper( localName = "ExecuteRawCommandResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ExecuteRawCommandResponse" )
	public abstract String executeRawCommand( @WebParam( name = "command", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString1, @WebParam( name = "params", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfPair paramArrayOfPair, @WebParam( name = "data", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString2 );

	@WebMethod( operationName = "GetCertificateChallenges", action = "http://marchnetworks.com/device_ws/GetCertificateChallenges" )
	@WebResult( name = "challenges", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetCertificateChallenges", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetCertificateChallenges" )
	@ResponseWrapper( localName = "GetCertificateChallengesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetCertificateChallengesResponse" )
	public abstract ArrayOfCertificateChallenge getCertificateChallenges( @WebParam( name = "certificateIds", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );

	@WebMethod( operationName = "ValidateCertificate", action = "http://marchnetworks.com/device_ws/ValidateCertificate" )
	@WebResult( name = "bSuccess", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "ValidateCertificate", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ValidateCertificate" )
	@ResponseWrapper( localName = "ValidateCertificateResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ValidateCertificateResponse" )
	public abstract boolean validateCertificate( @WebParam( name = "certificateId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString1, @WebParam( name = "challengeValidation", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString2 );

	@WebMethod( operationName = "ExecuteFailsafeCommand", action = "http://marchnetworks.com/device_ws/ExecuteFailsafeCommand" )
	@RequestWrapper( localName = "ExecuteFailsafeCommand", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ExecuteFailsafeCommand" )
	@ResponseWrapper( localName = "ExecuteFailsafeCommandResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ExecuteFailsafeCommandResponse" )
	public abstract void executeFailsafeCommand( @WebParam( name = "command", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString1, @WebParam( name = "params", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString2 );

	@WebMethod( operationName = "ImportLicense", action = "http://marchnetworks.com/device_ws/ImportLicense" )
	@RequestWrapper( localName = "ImportLicense", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ImportLicense" )
	@ResponseWrapper( localName = "ImportLicenseResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.ImportLicenseResponse" )
	public abstract void importLicense( @WebParam( name = "LicenseXml", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString );

	@WebMethod( operationName = "GetLicenseInfo", action = "http://marchnetworks.com/device_ws/GetLicenseInfo" )
	@WebResult( name = "GetLicenseInfoResponse", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetLicenseInfo", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetLicenseInfo" )
	@ResponseWrapper( localName = "GetLicenseInfoResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetLicenseInfoResponse" )
	public abstract LicenseInfo getLicenseInfo();

	@WebMethod( operationName = "GetAlarmSources", action = "http://marchnetworks.com/device_ws/GetAlarmSources" )
	@WebResult( name = "GetAlarmSourcesResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAlarmSources", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlarmSources" )
	@ResponseWrapper( localName = "GetAlarmSourcesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlarmSourcesResponse" )
	public abstract ArrayOfAlarmSource getAlarmSources();

	@WebMethod( operationName = "CloseAlarmEntries", action = "http://marchnetworks.com/device_ws/CloseAlarmEntries" )
	@RequestWrapper( localName = "CloseAlarmEntries", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CloseAlarmEntries" )
	@ResponseWrapper( localName = "CloseAlarmEntriesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CloseAlarmEntriesResponse" )
	public abstract void closeAlarmEntries( @WebParam( name = "closeRecords", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfAlarmEntryCloseRecord paramArrayOfAlarmEntryCloseRecord );

	@WebMethod( operationName = "GetSwitches", action = "http://marchnetworks.com/device_ws/GetSwitches" )
	@WebResult( name = "GetSwitchesResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetSwitches", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetSwitches" )
	@ResponseWrapper( localName = "GetSwitchesResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetSwitchesResponse" )
	public abstract ArrayOfSwitch getSwitches();

	@WebMethod( operationName = "GetAlertConfig", action = "http://marchnetworks.com/device_ws/GetAlertConfig" )
	@WebResult( name = "GetAlertConfigResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAlertConfig", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertConfig" )
	@ResponseWrapper( localName = "GetAlertConfigResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertConfigResponse" )
	public abstract AlertConfig getAlertConfig();

	@WebMethod( operationName = "GetAlertConfigId", action = "http://marchnetworks.com/device_ws/GetAlertConfigId" )
	@WebResult( name = "GetAlertConfigIdResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAlertConfigId", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertConfigId" )
	@ResponseWrapper( localName = "GetAlertConfigIdResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertConfigIdResponse" )
	public abstract String getAlertConfigId();

	@WebMethod( operationName = "SetAlertConfig", action = "http://marchnetworks.com/device_ws/SetAlertConfig" )
	@RequestWrapper( localName = "SetAlertConfig", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetAlertConfig" )
	@ResponseWrapper( localName = "SetAlertConfigResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.SetAlertConfigResponse" )
	public abstract void setAlertConfig( @WebParam( name = "alertConfig", targetNamespace = "http://marchnetworks.com/device_ws/" ) AlertConfig paramAlertConfig );

	@WebMethod( operationName = "GetAlerts", action = "http://marchnetworks.com/device_ws/GetAlerts" )
	@WebResult( name = "GetAlertsResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAlerts", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlerts" )
	@ResponseWrapper( localName = "GetAlertsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertsResponse" )
	public abstract ArrayOfAlertEntry getAlerts();

	@WebMethod( operationName = "GetAlert", action = "http://marchnetworks.com/device_ws/GetAlert" )
	@WebResult( name = "GetAlertResult", targetNamespace = "http://marchnetworks.com/device_ws/" )
	@RequestWrapper( localName = "GetAlert", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlert" )
	@ResponseWrapper( localName = "GetAlertResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.GetAlertResponse" )
	public abstract AlertEntry getAlert( @WebParam( name = "alertId", targetNamespace = "http://marchnetworks.com/device_ws/" ) String paramString );

	@WebMethod( operationName = "CloseAlerts", action = "http://marchnetworks.com/device_ws/CloseAlerts" )
	@RequestWrapper( localName = "CloseAlerts", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CloseAlerts" )
	@ResponseWrapper( localName = "CloseAlertsResponse", targetNamespace = "http://marchnetworks.com/device_ws/", className = "com.marchnetworks.device_ws.CloseAlertsResponse" )
	public abstract void closeAlerts( @WebParam( name = "alertIds", targetNamespace = "http://marchnetworks.com/device_ws/" ) ArrayOfString paramArrayOfString );
}
