package ru.iandreyshev.timemanager.domain

interface IRepository {
    suspend fun saveCard(card: Card): Card
    suspend fun saveEvent(cardId: CardId, event: Event): Event?

    suspend fun update(cardId: CardId, event: Event)

    suspend fun getEvent(id: EventId): Event?
    suspend fun getEvents(card: Card): List<Event>
    suspend fun getLastCard(): Card?
    suspend fun getNextCard(current: Card): Card?
    suspend fun getPreviousCard(current: Card): Card?
}