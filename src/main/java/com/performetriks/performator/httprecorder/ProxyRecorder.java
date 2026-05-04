package com.performetriks.performator.httprecorder;

import java.util.List;
import java.util.Map;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedSslEngineSource;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.performetriks.performator.conversion.RequestEntry;
import com.performetriks.performator.conversion.RequestModel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;



public class ProxyRecorder {

	public static final Logger logger = LoggerFactory.getLogger(ProxyRecorder.class);
	
    private final RequestModel requestModel;
    private final PFRHttpRecorderUI ui;
    
    private final SelfSignedSslEngineSource trustAll = new SelfSignedSslEngineSource(true);

    private HttpProxyServer server;
    
    public ProxyRecorder(PFRHttpRecorderUI ui,
                         RequestModel model) {
        this.ui = ui;
        this.requestModel = model;
    }
    
    public void stop() {
    	server.stop();
    }

    public ProxyRecorder start(int port) {

        server =
            DefaultHttpProxyServer.bootstrap()
                .withPort(port)
                .withManInTheMiddle(ImpersonatingMitmManager.builder().build())
                .withFiltersSource(new HttpFiltersSourceAdapter() {


                    public HttpFilters filterRequest(HttpRequest originalRequest,
                                                     ChannelHandlerContext ctx) {
                    	
                    	boolean isHttps = ctx.pipeline().get(SslHandler.class) != null;
                    	
                        return new HttpFiltersAdapter(originalRequest) {
                        	
                        	RequestEntry entry = new RequestEntry();

                            @Override
                            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                
                            	
                            	//---------------------------------
                            	// Request
                            	if (httpObject instanceof HttpRequest req) {
                            		
                            		//---------------------------------
                                	// Retrieve Method and URL
                            		HttpMethod method = req.getMethod();
                            		String uri = req.getUri();
                            		

                            		if(method == HttpMethod.CONNECT) {
                            			return null; // skip
                            		}
                            		
                            		System.out.println("Received Request: " + method + " " + uri);
                            		
                            		//---------------------------------
                                	// Add Server if missing
                            		if( ! uri.trim().toLowerCase().startsWith("http") ) {
                                		
                            			String host = req.headers().get("Host");                      			
                            	        String protocol = isHttps ? "https://" : "http://";
                            			
                            			uri = protocol + host + uri;
                            		}
                            		
                            		//---------------------------------
                                	// Decode Params
                            	    QueryStringDecoder decoder = new QueryStringDecoder(uri);

                            	    String path = decoder.path(); // /some/path
                            	    Map<String, List<String>> params = decoder.parameters(); // query params


                            	    
                            	    
                            	    //---------------------------
                                    // Headers
                                    for (Map.Entry<String, String> h : req.headers()) {
                                        entry.header(h.getKey(), h.getValue());
                                    }
                                    
                                    //---------------------------
                                    // Add entry
                                    entry.method(req.getMethod().name());
                            	    entry.setURL(uri);
                                    synchronized (requestModel) {
                                        requestModel.add(entry);
                                    }
                            	}
                                
                                
                                //---------------------------------
                            	// Body
                                if (httpObject instanceof HttpContent content) {

                                    String bodyPart = content.content().toString(io.netty.util.CharsetUtil.UTF_8);
                                    if(! entry.hasBody() ) {
                                    	entry.body(bodyPart);
                                    }else {
                                    	entry.body( entry.body() + bodyPart);
                                    }
                                }

                                javax.swing.SwingUtilities.invokeLater(() -> ui.regenerateCode());
                                return null; // continue processing
                            }

                            // ================= RESPONSE =================

                            @Override
                            public HttpObject serverToProxyResponse(HttpObject httpObject) {

//                                if (httpObject instanceof HttpResponse res && entry != null) {
//                                    entry.status = res.status().code();
//                                }

                                return httpObject;
                            }
                        };
                    }
                })
                .start();

        System.out.println("Proxy running on port " + port);
        
        return this;
    }
}
