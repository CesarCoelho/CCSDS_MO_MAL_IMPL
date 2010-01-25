/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccsds.moims.mo.mal.impl.provider;

import org.ccsds.moims.mo.mal.impl.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALPubSubOperation;
import org.ccsds.moims.mo.mal.impl.util.Logging;
import org.ccsds.moims.mo.mal.provider.MALPublishInteractionListener;
import org.ccsds.moims.mo.mal.provider.MALPublisher;
import org.ccsds.moims.mo.mal.provider.MALSubscriberEventListener;
import org.ccsds.moims.mo.mal.structures.DomainIdentifier;
import org.ccsds.moims.mo.mal.structures.EntityKey;
import org.ccsds.moims.mo.mal.structures.EntityKeyList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.StandardError;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UpdateList;

/**
 *
 * @author cooper_sf
 */
public class MALPublisherImpl implements MALPublisher
{
  private final MALProviderImpl parent;
  private final MessageSend handler;
  private final MALPubSubOperation operation;
  private Map<AddressKey, Identifier> transId = new TreeMap<AddressKey, Identifier>();

  public MALPublisherImpl(MALProviderImpl parent, MessageSend handler, MALPubSubOperation operation)
  {
    this.parent = parent;
    this.handler = handler;
    this.operation = operation;
  }

  @Override
  public void register(EntityKeyList entityKeys, MALPublishInteractionListener listener, DomainIdentifier domain, Identifier networkZone, SessionType sessionType, Identifier sessionName, QoSLevel remotePublisherQos, Hashtable remotePublisherQosProps, Integer remotePublisherPriority) throws MALException
  {
    MessageDetails details = new MessageDetails(parent.getPublishEndpoint(), parent.getURI(), null, parent.getBrokerURI(), operation.getService(), parent.getAuthenticationId(), domain, networkZone, sessionType, sessionName, remotePublisherQos, remotePublisherQosProps, remotePublisherPriority);

    setTransId(parent.getPublishEndpoint().getURI(), domain, networkZone.getValue(), sessionType, sessionName.getValue(), handler.publishRegister(details, operation, entityKeys, listener));
  }

  @Override
  public void asyncRegister(EntityKeyList entityKeys, MALPublishInteractionListener listener, DomainIdentifier domain, Identifier networkZone, SessionType sessionType, Identifier sessionName, QoSLevel remotePublisherQos, Hashtable remotePublisherQosProps, Integer remotePublisherPriority) throws MALException
  {
    MessageDetails details = new MessageDetails(parent.getPublishEndpoint(), parent.getURI(), null, parent.getBrokerURI(), operation.getService(), parent.getAuthenticationId(), domain, networkZone, sessionType, sessionName, remotePublisherQos, remotePublisherQosProps, remotePublisherPriority);

    setTransId(parent.getPublishEndpoint().getURI(), domain, networkZone.getValue(), sessionType, sessionName.getValue(), handler.publishRegisterAsync(details, operation, entityKeys, listener));
  }

  @Override
  public void publish(UpdateList updateList, DomainIdentifier domain, Identifier networkZone, SessionType sessionType, Identifier sessionName, QoSLevel publishQos, Hashtable publishQosProps, Integer publishPriority) throws MALException
  {
    MessageDetails details = new MessageDetails(parent.getPublishEndpoint(), parent.getURI(), null, parent.getBrokerURI(), operation.getService(), parent.getAuthenticationId(), domain, networkZone, sessionType, sessionName, publishQos, publishQosProps, publishPriority);

    Identifier tid = getTransId(parent.getPublishEndpoint().getURI(), domain, networkZone.getValue(), sessionType, sessionName.getValue());

    if (null != tid)
    {
      Logging.logMessage("INFO: Publisher using transaction Id of: " + tid);

      handler.publish(details, tid, operation, updateList);
    }
    else
    {
      // this means that we haven't successfully registered, need to throw an exception
      throw new MALException(new StandardError(MALHelper.INCORRECT_STATE_ERROR_NUMBER, null));
    }
  }

  @Override
  public void addPublishedEntityKeys(EntityKey[] keys) throws MALException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removePublishedEntityKeys(EntityKey[] keys) throws MALException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isListened(EntityKey entityKey) throws MALException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setSubscriberEventListener(MALSubscriberEventListener listener) throws MALException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deregister(DomainIdentifier domain, Identifier networkZone, SessionType sessionType, Identifier sessionName, QoSLevel remotePublisherQos, Hashtable remotePublisherQosProps, Integer remotePublisherPriority) throws MALException
  {
    MessageDetails details = new MessageDetails(parent.getPublishEndpoint(), parent.getURI(), null, parent.getBrokerURI(), operation.getService(), parent.getAuthenticationId(), domain, networkZone, sessionType, sessionName, remotePublisherQos, remotePublisherQosProps, remotePublisherPriority);

    handler.publishDeregister(details, operation);

    clearTransId(parent.getPublishEndpoint().getURI(), domain, networkZone.getValue(), sessionType, sessionName.getValue());
  }

  @Override
  public void asyncDeregister(MALPublishInteractionListener listener, DomainIdentifier domain, Identifier networkZone, SessionType sessionType, Identifier sessionName, QoSLevel remotePublisherQos, Hashtable remotePublisherQosProps, Integer remotePublisherPriority) throws MALException
  {
    MessageDetails details = new MessageDetails(parent.getPublishEndpoint(), parent.getURI(), null, parent.getBrokerURI(), operation.getService(), parent.getAuthenticationId(), domain, networkZone, sessionType, sessionName, remotePublisherQos, remotePublisherQosProps, remotePublisherPriority);

    handler.publishDeregisterAsync(details, operation, listener);

    clearTransId(parent.getPublishEndpoint().getURI(), domain, networkZone.getValue(), sessionType, sessionName.getValue());
  }

  private synchronized void setTransId(URI brokerUri, DomainIdentifier domain, String networkZone, SessionType session, String sessionName, Identifier id)
  {
    AddressKey key = new AddressKey(brokerUri, domain, networkZone, session, sessionName);

    if (!transId.containsKey(key))
    {
      Logging.logMessage("INFO: Publisher setting transaction Id to: " + id);
      transId.put(key, id);
    }
  }

  private synchronized void clearTransId(URI brokerUri, DomainIdentifier domain, String networkZone, SessionType session, String sessionName)
  {
    AddressKey key = new AddressKey(brokerUri, domain, networkZone, session, sessionName);

    Identifier id = transId.get(key);
    if (null != id)
    {
      Logging.logMessage("INFO: Publisher removing transaction Id of: " + id);
      transId.remove(key);
    }
  }

  private synchronized Identifier getTransId(URI brokerUri, DomainIdentifier domain, String networkZone, SessionType session, String sessionName)
  {
    return transId.get(new AddressKey(brokerUri, domain, networkZone, session, sessionName));
  }
}