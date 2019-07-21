/* ----------------------------------------------------------------------------
 * Copyright (C) 2013      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO MAL Java Implementation
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.mal.impl.patterns;

import esa.mo.mal.impl.Address;
import esa.mo.mal.impl.MessageSend;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALRequestOperation;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.provider.MALRequest;
import org.ccsds.moims.mo.mal.transport.MALEncodedBody;
import org.ccsds.moims.mo.mal.transport.MALMessage;

/**
 * Request interaction class.
 */
public class RequestInteractionImpl extends BaseInteractionImpl implements MALRequest
{
  /**
   * Constructor.
   * @param sender Used to return the messages.
   * @param address Details of this endpoint.
   * @param msg The source message.
   * @throws MALInteractionException if the received message operation is unknown.
   */
  public RequestInteractionImpl(final MessageSend sender,
          final Address address,
          final MALMessage msg) throws MALInteractionException
  {
    super(sender, address, msg);
  }

  @Override
  public MALMessage sendResponse(final Object... result) throws MALInteractionException, MALException
  {
    return returnResponse(MALRequestOperation.REQUEST_RESPONSE_STAGE, result);
  }

  @Override
  public MALMessage sendResponse(final MALEncodedBody body) throws MALInteractionException, MALException
  {
    return returnResponse(MALRequestOperation.REQUEST_RESPONSE_STAGE, body);
  }

  @Override
  public MALMessage sendError(final MALStandardError error) throws MALException
  {
    return returnError(MALRequestOperation.REQUEST_RESPONSE_STAGE, error);
  }
}
