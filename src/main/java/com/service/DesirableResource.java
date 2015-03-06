package com.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import com.db.RelationshipTypeDB;
import com.db.TradingEntityDB;
import com.db.UserDB;
import com.exceptions.ObjectMappingException;
import com.factory.LabelFactory;
import com.factory.RelationshipTypeFactory;
import com.objects.domain.TradingEntity;
import com.objects.mapping.ObjectManager;

@Path("/desirable")
public class DesirableResource {
    @POST
    @Path("/addDesirable")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDesirable(@Context HttpHeaders headers, String tradingEntity) {
    	String auth = headers.getRequestHeaders().getFirst("authorization");
    	try {
    		Node userNode = UserDB.getUserNode(auth);
			TradingEntity newTradingEntity = (TradingEntity) ObjectManager.readObjectAsString(tradingEntity, TradingEntity.class);
			Label tradingEntityLabel = LabelFactory.getLabel(newTradingEntity.getLabel());
			Node tradingEntityNode = TradingEntityDB.getOrCreateTradingEntity(newTradingEntity.getName(), LabelFactory.BeerLabels.DESIRABLE, tradingEntityLabel);
			
			RelationshipTypeDB.addRelationshipBetweenNodes(userNode, tradingEntityNode, RelationshipTypeFactory.BeerRelationships.DESIRES);
			if (newTradingEntity.getRelations()!=null) {
				for (TradingEntity relatedTradingEntity: newTradingEntity.getRelations()) {
					Label relatedTradingEntityLabel = LabelFactory.getLabel(relatedTradingEntity.getLabel());
					Node relatedTradingEntityNode = TradingEntityDB.getOrCreateTradingEntity(relatedTradingEntity.getName(), LabelFactory.getLabel(relatedTradingEntity.getLabel()));
					RelationshipType newRelation = RelationshipTypeFactory.getRelationshipType(tradingEntityLabel, relatedTradingEntityLabel);
;					RelationshipTypeDB.addRelationshipBetweenNodes(tradingEntityNode, relatedTradingEntityNode, newRelation);
				}
			}
			return Response.status(200).entity(newTradingEntity.getName()).build();
    	} catch(ObjectMappingException e) {
	    	System.out.println(e.getMessage());
	    	return Response.status(400).entity(e.getMessage()).build();
	    }
    }
    
    @POST
    @Path("/removeDesirable")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeDesirable(@Context HttpHeaders headers, String tradingEntity) {
    	String auth = headers.getRequestHeaders().getFirst("authorization");
    	try {
    		Node userNode = UserDB.getUserNode(auth);
    		TradingEntity entity = (TradingEntity) ObjectManager.readObjectAsString(tradingEntity, TradingEntity.class);
    		Label tradingEntityLabel = LabelFactory.getLabel(entity.getLabel());
    		Node tradingEntityNode = TradingEntityDB.getTradingEntity(entity.getName(), tradingEntityLabel);
    		RelationshipTypeDB.removeRelationship(userNode, tradingEntityNode, RelationshipTypeFactory.BeerRelationships.DESIRES);
    		
    		return Response.status(200).entity(entity.getName()).build();
    	} catch(ObjectMappingException e) {
	    	System.out.println(e.getMessage());
	    	return Response.status(400).entity(e.getMessage()).build();
	    }
    }
}