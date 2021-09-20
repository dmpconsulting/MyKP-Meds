package com.montunosoftware.pillpopper.model;

public interface ItemFactory<ItemType>
{
	ItemType create(String id, String name, StateUpdatedListener stateUpdatedListener);
}
