package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.zenith.api.hub.HubService;

import java.util.stream.Collectors;

@Provider
public class RunningSessionDataSet extends MemoryDataSet<RunningSession, PojoField<RunningSession>, String> {

    public RunningSessionDataSet(DataSetFactory<RunningSession, PojoField<RunningSession>, String> factory, Metadata<RunningSession, PojoField<RunningSession>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<RunningSession> extractModels(Filter filterable) {
        HubService hubService = getService(HubService.class);
        return hubService.getSessions().stream().map(RunningSession::from).collect(Collectors.toList());
    }
}
