package fr.trandutrieu.remy.socle.soap;

import javax.jws.HandlerChain;

@HandlerChain(file = "/handlers.xml")
public abstract class WebserviceImpl implements Webservice {

}
