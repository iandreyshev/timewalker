package ru.iandreyshev.timemanager.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import ru.iandreyshev.timemanager.domain.*

class Repository(
    private val cardDao: ICardDao,
    private val eventDao: IEventDao
) : IRepository {

    override suspend fun saveCard(card: Card): Card {
        return withContext(Dispatchers.Default) {
            val order = (cardDao.lastOrder() ?: 0) + 1
            val entity = CardEntity(
                title = "Card #$order",
                order = order
            )
            val id = cardDao.insert(entity)

            return@withContext Card(
                id = CardId(id),
                title = entity.title,
                date = ZonedDateTime.now()
            )
        }
    }

    override suspend fun saveEvent(cardId: CardId, event: Event): RepoResult<Event> {
        return withContext(Dispatchers.Default) {
            cardDao.get(cardId.value)
                ?: return@withContext RepoResult.Error(RepoError.Unknown)

            var eventToSave = event
            val previousEvent = getEvents(cardId).firstOrNull()

            eventToSave = if (previousEvent != null) {
                eventToSave.copy(startDateTime = previousEvent.endDateTime)
            } else {
                eventToSave.copy(isFirstInCard = true)
            }

            val entity = EventEntity.create(cardId, eventToSave)
            entity.id = 0
            val id = eventDao.insert(entity)

            return@withContext RepoResult.Success(
                data = eventToSave.copy(id = EventId(id))
            )
        }
    }

    override suspend fun update(cardId: CardId, event: Event): RepoResult<Unit> {
        return withContext(Dispatchers.Default) {
            val cardEntity = cardDao.get(cardId.value)
                ?: return@withContext RepoResult.Error(RepoError.Unknown)
            val updateEntity = EventEntity.create(cardEntity.id, event)

            eventDao.update(updateEntity)

            return@withContext RepoResult.Success(Unit)
        }
    }

    override suspend fun getEvent(id: EventId): Event? {
        return withContext(Dispatchers.Default) {
            val entity = eventDao.get(id.value) ?: return@withContext null

            Event(
                id = EventId(entity.id),
                description = entity.description,
                startDateTime = entity.startTime,
                endDateTime = entity.endTime,
                isFirstInCard = entity.isFirstInCard
            )
        }
    }

    override suspend fun getEvents(cardId: CardId): List<Event> {
        return withContext(Dispatchers.Default) {
            eventDao.getAll(cardId.value)
                .map { entity ->
                    Event(
                        id = EventId(entity.id),
                        description = entity.description,
                        startDateTime = entity.startTime,
                        endDateTime = entity.endTime,
                        isFirstInCard = entity.isFirstInCard
                    )
                }
        }
    }

    override suspend fun getEventsCount(cardId: CardId): Int {
        return withContext(Dispatchers.Default) {
            eventDao.getAll(cardId.value).count()
        }
    }

    override suspend fun getNextCard(current: Card): Card? {
        return withContext(Dispatchers.Default) {
            val entity = cardDao.get(current.id.value) ?: return@withContext null
            val next = cardDao.getNext(entity.order) ?: return@withContext null

            Card(
                id = CardId(next.id),
                title = next.title,
                date = ZonedDateTime.now()
            )
        }
    }

    override suspend fun getPreviousCard(current: Card): Card? {
        return withContext(Dispatchers.Default) {
            val entity = cardDao.get(current.id.value) ?: return@withContext null
            val previous = cardDao.getPrevious(entity.order) ?: return@withContext null

            Card(
                id = CardId(previous.id),
                title = previous.title,
                date = ZonedDateTime.now()
            )
        }
    }

    override suspend fun getLastCard(): Card? {
        return withContext(Dispatchers.Default) {
            val entity = cardDao.getLast() ?: return@withContext null

            Card(
                id = CardId(entity.id),
                title = entity.title,
                date = ZonedDateTime.now()
            )
        }
    }

    override suspend fun deleteCard(cardId: CardId): RepoResult<Unit> {
        return withContext(Dispatchers.Default) {
            cardDao.delete(cardId.value)
            eventDao.delete(cardId.value)
            return@withContext RepoResult.Success(Unit)
        }
    }

}
