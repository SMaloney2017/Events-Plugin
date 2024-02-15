package com.commands;

abstract public class CommandAbstract
{
	abstract public void initialize();

	abstract public boolean validate();

	abstract public void execute();

	abstract public void reset();
}
