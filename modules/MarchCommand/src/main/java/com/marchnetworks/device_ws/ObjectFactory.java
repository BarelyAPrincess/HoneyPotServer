package com.marchnetworks.device_ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory
{
	private static final QName _SubscribeEventsNotifyOfflineSeqNo_QNAME = new QName( "http://marchnetworks.com/device_ws/", "offlineSeqNo" );
	private static final QName _RegisterServerAddresses_QNAME = new QName( "http://marchnetworks.com/device_ws/", "serverAddresses" );
	private static final QName _RegisterServerHostname_QNAME = new QName( "http://marchnetworks.com/device_ws/", "serverHostname" );
	private static final QName _RegisterServerAddress_QNAME = new QName( "http://marchnetworks.com/device_ws/", "serverAddress" );

	public CloseAlarmEntries createCloseAlarmEntries()
	{
		return new CloseAlarmEntries();
	}

	public ArrayOfAlarmEntryCloseRecord createArrayOfAlarmEntryCloseRecord()
	{
		return new ArrayOfAlarmEntryCloseRecord();
	}

	public GetAlertConfigId createGetAlertConfigId()
	{
		return new GetAlertConfigId();
	}

	public GetTime createGetTime()
	{
		return new GetTime();
	}

	public CancelRequestsResponse createCancelRequestsResponse()
	{
		return new CancelRequestsResponse();
	}

	public GetAllChannelDetailsResponse createGetAllChannelDetailsResponse()
	{
		return new GetAllChannelDetailsResponse();
	}

	public ArrayOfChannelDetails createArrayOfChannelDetails()
	{
		return new ArrayOfChannelDetails();
	}

	public GetAlertsResponse createGetAlertsResponse()
	{
		return new GetAlertsResponse();
	}

	public ArrayOfAlertEntry createArrayOfAlertEntry()
	{
		return new ArrayOfAlertEntry();
	}

	public GetAlertConfigResponse createGetAlertConfigResponse()
	{
		return new GetAlertConfigResponse();
	}

	public AlertConfig createAlertConfig()
	{
		return new AlertConfig();
	}

	public SetParametersResponse createSetParametersResponse()
	{
		return new SetParametersResponse();
	}

	public GetCertificateChallengesResponse createGetCertificateChallengesResponse()
	{
		return new GetCertificateChallengesResponse();
	}

	public ArrayOfCertificateChallenge createArrayOfCertificateChallenge()
	{
		return new ArrayOfCertificateChallenge();
	}

	public SetAlertConfig createSetAlertConfig()
	{
		return new SetAlertConfig();
	}

	public GetServiceCapabilitiesResponse createGetServiceCapabilitiesResponse()
	{
		return new GetServiceCapabilitiesResponse();
	}

	public Capabilities createCapabilities()
	{
		return new Capabilities();
	}

	public ImportLicenseResponse createImportLicenseResponse()
	{
		return new ImportLicenseResponse();
	}

	public GetChannelDetails createGetChannelDetails()
	{
		return new GetChannelDetails();
	}

	public GetParametersResponse createGetParametersResponse()
	{
		return new GetParametersResponse();
	}

	public GetParametersResult createGetParametersResult()
	{
		return new GetParametersResult();
	}

	public GetChannelConfigHashesResponse createGetChannelConfigHashesResponse()
	{
		return new GetChannelConfigHashesResponse();
	}

	public ArrayOfHashResult createArrayOfHashResult()
	{
		return new ArrayOfHashResult();
	}

	public GetAlarmSourcesResponse createGetAlarmSourcesResponse()
	{
		return new GetAlarmSourcesResponse();
	}

	public ArrayOfAlarmSource createArrayOfAlarmSource()
	{
		return new ArrayOfAlarmSource();
	}

	public CloseAlerts createCloseAlerts()
	{
		return new CloseAlerts();
	}

	public ArrayOfString createArrayOfString()
	{
		return new ArrayOfString();
	}

	public ValidateCertificateResponse createValidateCertificateResponse()
	{
		return new ValidateCertificateResponse();
	}

	public Subscribe createSubscribe()
	{
		return new Subscribe();
	}

	public SetCertificateChain createSetCertificateChain()
	{
		return new SetCertificateChain();
	}

	public GetAlarmSources createGetAlarmSources()
	{
		return new GetAlarmSources();
	}

	public GetChannelDetailsResponse createGetChannelDetailsResponse()
	{
		return new GetChannelDetailsResponse();
	}

	public ChannelDetails createChannelDetails()
	{
		return new ChannelDetails();
	}

	public GetAlertResponse createGetAlertResponse()
	{
		return new GetAlertResponse();
	}

	public AlertEntry createAlertEntry()
	{
		return new AlertEntry();
	}

	public Register createRegister()
	{
		return new Register();
	}

	public GetAlertConfig createGetAlertConfig()
	{
		return new GetAlertConfig();
	}

	public GetWaitingEvents createGetWaitingEvents()
	{
		return new GetWaitingEvents();
	}

	public GetAlertConfigIdResponse createGetAlertConfigIdResponse()
	{
		return new GetAlertConfigIdResponse();
	}

	public SubscribeResponse createSubscribeResponse()
	{
		return new SubscribeResponse();
	}

	public GetAlert createGetAlert()
	{
		return new GetAlert();
	}

	public GetChannelConfigHashes createGetChannelConfigHashes()
	{
		return new GetChannelConfigHashes();
	}

	public GetServiceCapabilities createGetServiceCapabilities()
	{
		return new GetServiceCapabilities();
	}

	public GetAudioOutputs createGetAudioOutputs()
	{
		return new GetAudioOutputs();
	}

	public RegisterResponse createRegisterResponse()
	{
		return new RegisterResponse();
	}

	public GetAudioOutputsResponse createGetAudioOutputsResponse()
	{
		return new GetAudioOutputsResponse();
	}

	public ArrayOfAudioOutput createArrayOfAudioOutput()
	{
		return new ArrayOfAudioOutput();
	}

	public ExecuteRawCommand createExecuteRawCommand()
	{
		return new ExecuteRawCommand();
	}

	public ArrayOfPair createArrayOfPair()
	{
		return new ArrayOfPair();
	}

	public UnregisterResponse createUnregisterResponse()
	{
		return new UnregisterResponse();
	}

	public CancelRequests createCancelRequests()
	{
		return new CancelRequests();
	}

	public GetSwitches createGetSwitches()
	{
		return new GetSwitches();
	}

	public CloseAlarmEntriesResponse createCloseAlarmEntriesResponse()
	{
		return new CloseAlarmEntriesResponse();
	}

	public SubscribeEventsNotifyResponse createSubscribeEventsNotifyResponse()
	{
		return new SubscribeEventsNotifyResponse();
	}

	public ModifySubscriptionResponse createModifySubscriptionResponse()
	{
		return new ModifySubscriptionResponse();
	}

	public GetAllChannelDetails createGetAllChannelDetails()
	{
		return new GetAllChannelDetails();
	}

	public GetConfigHashResponse createGetConfigHashResponse()
	{
		return new GetConfigHashResponse();
	}

	public UnsubscribeResponse createUnsubscribeResponse()
	{
		return new UnsubscribeResponse();
	}

	public GetParameters createGetParameters()
	{
		return new GetParameters();
	}

	public SetCertificateChainResponse createSetCertificateChainResponse()
	{
		return new SetCertificateChainResponse();
	}

	public SetParameters createSetParameters()
	{
		return new SetParameters();
	}

	public ArrayOfGenericParameter createArrayOfGenericParameter()
	{
		return new ArrayOfGenericParameter();
	}

	public GetLicenseInfoResponse createGetLicenseInfoResponse()
	{
		return new GetLicenseInfoResponse();
	}

	public LicenseInfo createLicenseInfo()
	{
		return new LicenseInfo();
	}

	public ModifySubscription createModifySubscription()
	{
		return new ModifySubscription();
	}

	public ValidateCertificate createValidateCertificate()
	{
		return new ValidateCertificate();
	}

	public GetSystemDetails createGetSystemDetails()
	{
		return new GetSystemDetails();
	}

	public UpdateRegistrationDetailsResponse createUpdateRegistrationDetailsResponse()
	{
		return new UpdateRegistrationDetailsResponse();
	}

	public GetAlerts createGetAlerts()
	{
		return new GetAlerts();
	}

	public SubscribeEventsNotify createSubscribeEventsNotify()
	{
		return new SubscribeEventsNotify();
	}

	public ExecuteRawCommandResponse createExecuteRawCommandResponse()
	{
		return new ExecuteRawCommandResponse();
	}

	public GetRegistrationDetails createGetRegistrationDetails()
	{
		return new GetRegistrationDetails();
	}

	public SetAlertConfigResponse createSetAlertConfigResponse()
	{
		return new SetAlertConfigResponse();
	}

	public Unregister createUnregister()
	{
		return new Unregister();
	}

	public GetWaitingEventsResponse createGetWaitingEventsResponse()
	{
		return new GetWaitingEventsResponse();
	}

	public ArrayOfEvent createArrayOfEvent()
	{
		return new ArrayOfEvent();
	}

	public GetSwitchesResponse createGetSwitchesResponse()
	{
		return new GetSwitchesResponse();
	}

	public ArrayOfSwitch createArrayOfSwitch()
	{
		return new ArrayOfSwitch();
	}

	public GetSystemDetailsResponse createGetSystemDetailsResponse()
	{
		return new GetSystemDetailsResponse();
	}

	public DeviceDetails createDeviceDetails()
	{
		return new DeviceDetails();
	}

	public ExecuteFailsafeCommand createExecuteFailsafeCommand()
	{
		return new ExecuteFailsafeCommand();
	}

	public ExecuteFailsafeCommandResponse createExecuteFailsafeCommandResponse()
	{
		return new ExecuteFailsafeCommandResponse();
	}

	public CloseAlertsResponse createCloseAlertsResponse()
	{
		return new CloseAlertsResponse();
	}

	public GetTimeResponse createGetTimeResponse()
	{
		return new GetTimeResponse();
	}

	public Timestamp createTimestamp()
	{
		return new Timestamp();
	}

	public GetCertificateChallenges createGetCertificateChallenges()
	{
		return new GetCertificateChallenges();
	}

	public ImportLicense createImportLicense()
	{
		return new ImportLicense();
	}

	public GetConfigHash createGetConfigHash()
	{
		return new GetConfigHash();
	}

	public UpdateRegistrationDetails createUpdateRegistrationDetails()
	{
		return new UpdateRegistrationDetails();
	}

	public Unsubscribe createUnsubscribe()
	{
		return new Unsubscribe();
	}

	public GetRegistrationDetailsResponse createGetRegistrationDetailsResponse()
	{
		return new GetRegistrationDetailsResponse();
	}

	public RegistrationDetails createRegistrationDetails()
	{
		return new RegistrationDetails();
	}

	public GetLicenseInfo createGetLicenseInfo()
	{
		return new GetLicenseInfo();
	}

	public Switch createSwitch()
	{
		return new Switch();
	}

	public AlertThreshold createAlertThreshold()
	{
		return new AlertThreshold();
	}

	public GenericDouble createGenericDouble()
	{
		return new GenericDouble();
	}

	public AlarmSource createAlarmSource()
	{
		return new AlarmSource();
	}

	public LocalZone createLocalZone()
	{
		return new LocalZone();
	}

	public AddressZone createAddressZone()
	{
		return new AddressZone();
	}

	public CertificateChallenge createCertificateChallenge()
	{
		return new CertificateChallenge();
	}

	public GenericString createGenericString()
	{
		return new GenericString();
	}

	public Event createEvent()
	{
		return new Event();
	}

	public ArrayOfTextDetails createArrayOfTextDetails()
	{
		return new ArrayOfTextDetails();
	}

	public AudioOutput createAudioOutput()
	{
		return new AudioOutput();
	}

	public GenericInt64 createGenericInt64()
	{
		return new GenericInt64();
	}

	public ArrayOfAlertThreshold createArrayOfAlertThreshold()
	{
		return new ArrayOfAlertThreshold();
	}

	public DataDetails createDataDetails()
	{
		return new DataDetails();
	}

	public TimeZoneInfo createTimeZoneInfo()
	{
		return new TimeZoneInfo();
	}

	public ArrayOfGenericValue createArrayOfGenericValue()
	{
		return new ArrayOfGenericValue();
	}

	public VideoDetails createVideoDetails()
	{
		return new VideoDetails();
	}

	public ArrayOfAudioDetails createArrayOfAudioDetails()
	{
		return new ArrayOfAudioDetails();
	}

	public ArrayOfAddressZone createArrayOfAddressZone()
	{
		return new ArrayOfAddressZone();
	}

	public Pair createPair()
	{
		return new Pair();
	}

	public AudioDetails createAudioDetails()
	{
		return new AudioDetails();
	}

	public AddressZones createAddressZones()
	{
		return new AddressZones();
	}

	public AlarmEntryCloseRecord createAlarmEntryCloseRecord()
	{
		return new AlarmEntryCloseRecord();
	}

	public GenericInt32 createGenericInt32()
	{
		return new GenericInt32();
	}

	public ConfigurationURL createConfigurationURL()
	{
		return new ConfigurationURL();
	}

	public HashResult createHashResult()
	{
		return new HashResult();
	}

	public GenericParameter createGenericParameter()
	{
		return new GenericParameter();
	}

	public GenericParameterDeleteAction createGenericParameterDeleteAction()
	{
		return new GenericParameterDeleteAction();
	}

	public GenericNull createGenericNull()
	{
		return new GenericNull();
	}

	public ArrayOfVideoDetails createArrayOfVideoDetails()
	{
		return new ArrayOfVideoDetails();
	}

	public GenericValue createGenericValue()
	{
		return new GenericValue();
	}

	public GenericBoolean createGenericBoolean()
	{
		return new GenericBoolean();
	}

	public ArrayOfDataDetails createArrayOfDataDetails()
	{
		return new ArrayOfDataDetails();
	}

	public TextDetails createTextDetails()
	{
		return new TextDetails();
	}

	@XmlElementDecl( namespace = "http://marchnetworks.com/device_ws/", name = "offlineSeqNo", scope = SubscribeEventsNotify.class )
	public JAXBElement<Long> createSubscribeEventsNotifyOfflineSeqNo( Long value )
	{
		return new JAXBElement( _SubscribeEventsNotifyOfflineSeqNo_QNAME, Long.class, SubscribeEventsNotify.class, value );
	}

	@XmlElementDecl( namespace = "http://marchnetworks.com/device_ws/", name = "serverAddresses", scope = Register.class )
	public JAXBElement<ArrayOfString> createRegisterServerAddresses( ArrayOfString value )
	{
		return new JAXBElement( _RegisterServerAddresses_QNAME, ArrayOfString.class, Register.class, value );
	}

	@XmlElementDecl( namespace = "http://marchnetworks.com/device_ws/", name = "serverHostname", scope = Register.class )
	public JAXBElement<ArrayOfString> createRegisterServerHostname( ArrayOfString value )
	{
		return new JAXBElement( _RegisterServerHostname_QNAME, ArrayOfString.class, Register.class, value );
	}

	@XmlElementDecl( namespace = "http://marchnetworks.com/device_ws/", name = "serverAddress", scope = Register.class )
	public JAXBElement<ArrayOfString> createRegisterServerAddress( ArrayOfString value )
	{
		return new JAXBElement( _RegisterServerAddress_QNAME, ArrayOfString.class, Register.class, value );
	}

	@XmlElementDecl( namespace = "http://marchnetworks.com/device_ws/", name = "offlineSeqNo", scope = Subscribe.class )
	public JAXBElement<Long> createSubscribeOfflineSeqNo( Long value )
	{
		return new JAXBElement( _SubscribeEventsNotifyOfflineSeqNo_QNAME, Long.class, Subscribe.class, value );
	}
}
