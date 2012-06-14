package com.qcadoo.mes.states;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.joinArgs;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public abstract class StateChangeTest {

    @Mock
    protected Entity stateChangeEntity;

    @Mock
    protected StateChangeContext stateChangeContext;

    @Mock
    protected DataDefinition stateChangeDD;

    protected static final StateChangeEntityDescriber DESCRIBER = new MockStateChangeDescriber();

    protected void stubStateChangeEntity(final StateChangeEntityDescriber describer) {
        given(stateChangeEntity.getDataDefinition()).willReturn(stateChangeDD);
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(emptyEntityList);
        stubEntityField(stateChangeEntity, describer.getStatusFieldName(), StateChangeStatus.IN_PROGRESS.getStringValue());
    }

    protected EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

    protected Entity mockMessage(final MessageType type, final String translationKey, final String... translationArgs) {
        final Entity message = mock(Entity.class);
        stubEntityField(message, MessageFields.TYPE, type);
        stubEntityField(message, MessageFields.TRANSLATION_KEY, translationKey);
        stubEntityField(message, MessageFields.TRANSLATION_ARGS, joinArgs(translationArgs));
        return message;
    }

    protected static void mockStateChangeStatus(final Entity entity, final StateChangeStatus status) {
        stubEntityField(entity, DESCRIBER.getStatusFieldName(), status.getStringValue());
    }

    protected static void stubEntityField(final Entity entity, final String fieldName, final Object fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getStringField(fieldName)).willReturn(fieldValue == null ? null : fieldValue.toString());
    }

    protected void stubStateChangeContext() {
        given(stateChangeContext.getEntity()).willReturn(stateChangeEntity);
        given(stateChangeContext.getDescriber()).willReturn(DESCRIBER);

        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                stateChangeEntity.setField(DESCRIBER.getStatusFieldName(), invocation.getArguments()[0]);
                return null;
            }
        }).when(stateChangeContext).setStatus(Mockito.any(StateChangeStatus.class));

        given(stateChangeContext.getStatus()).willAnswer(new Answer<StateChangeStatus>() {

            @Override
            public StateChangeStatus answer(final InvocationOnMock invocation) throws Throwable {
                return StateChangeStatus.parseString(stateChangeEntity.getStringField(DESCRIBER.getStatusFieldName()));
            }
        });

        given(stateChangeContext.getAllMessages()).willAnswer(new Answer<List<Entity>>() {

            @Override
            public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName());
            }
        });

        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                stateChangeEntity.setField(DESCRIBER.getPhaseFieldName(), invocation.getArguments()[0]);
                return null;
            }
        }).when(stateChangeContext).setPhase(Mockito.anyInt());

        given(stateChangeContext.getPhase()).willAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable {
                return (Integer) stateChangeEntity.getField(DESCRIBER.getPhaseFieldName());
            }
        });
    }

}