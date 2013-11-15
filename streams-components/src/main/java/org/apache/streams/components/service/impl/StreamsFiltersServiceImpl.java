package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.StreamsFiltersService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class StreamsFiltersServiceImpl implements StreamsFiltersService {
    private static final Log LOG = LogFactory.getLog(StreamsFiltersServiceImpl.class);

    private SubscriptionRepository subscriptionRepository;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;
    private ObjectMapper mapper;

    @Autowired
    public StreamsFiltersServiceImpl(SubscriptionRepository subscriptionRepository, ActivityStreamsSubscriberWarehouse subscriberWarehouse) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriberWarehouse = subscriberWarehouse;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String getFilters(String subscriberId) throws Exception{
        ActivityStreamsSubscription subscription = subscriptionRepository.getSubscriptionByInRoute(subscriberId);
        return mapper.writeValueAsString(subscription.getFilters());
    }

    @Override
    public String updateFilters(String subscriberId, String tagsJson) throws Exception {
        LOG.info("updating filters for " + subscriberId);

        Map<String, List> updateFilters = (Map<String, List>) mapper.readValue(tagsJson, Map.class);
        subscriptionRepository.updateFilters(subscriberId, new HashSet<String>(updateFilters.get("add")), new HashSet<String>(updateFilters.get("remove")));
        subscriberWarehouse.getSubscriber(subscriberId).setLastUpdated(new Date(0));
        subscriberWarehouse.updateSubscriber(subscriptionRepository.getSubscriptionByInRoute(subscriberId));

        return "Filters Updated Successfully!";
    }
}