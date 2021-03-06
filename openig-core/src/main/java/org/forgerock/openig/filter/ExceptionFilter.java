/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2010–2011 ApexIdentity Inc.
 * Portions Copyright 2011-2014 ForgeRock AS.
 */

package org.forgerock.openig.filter;

import static org.forgerock.util.Utils.closeSilently;

import java.io.IOException;

import org.forgerock.openig.handler.Handler;
import org.forgerock.openig.handler.HandlerException;
import org.forgerock.openig.heap.HeapException;
import org.forgerock.openig.heap.HeapUtil;
import org.forgerock.openig.heap.NestedHeaplet;
import org.forgerock.openig.http.Exchange;
import org.forgerock.openig.log.LogTimer;

/**
 * Catches any exceptions thrown during handing of a request. This allows friendlier error
 * pages to be displayed than would otherwise be displayed by the container. Caught exceptions
 * are logged with a log level of {@link org.forgerock.openig.log.LogLevel#WARNING} and the exchange is diverted to
 * the specified exception handler.
 * <p>
 * Note: While the response object will be retained in the exchange object, this class will
 * close any open entity within the response object prior to dispatching the exchange to the
 * exception handler.
 */
public class ExceptionFilter extends GenericFilter {

    /** Handler to dispatch to in the event of caught exceptions. */
    private final Handler handler;

    /**
     * Build a new exception filter that will divert the flow to the given handler in case of exception.
     * @param handler exception handler
     */
    public ExceptionFilter(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public void filter(Exchange exchange, Handler next) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        try {
            next.handle(exchange);
        } catch (Throwable t) {
            // user-impacting
            logger.warning(t);
            closeSilently(exchange.response);
            handler.handle(exchange);
        }
        timer.stop();
    }

    /**
     * Creates and initializes an exception filter in a heap environment.
     */
    public static class Heaplet extends NestedHeaplet {
        @Override
        public Object create() throws HeapException {
            return new ExceptionFilter(HeapUtil.getRequiredObject(heap, config.get("handler"), Handler.class));
        }
    }
}
