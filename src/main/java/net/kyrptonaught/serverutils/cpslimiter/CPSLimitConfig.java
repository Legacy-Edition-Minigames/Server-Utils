package net.kyrptonaught.serverutils.cpslimiter;


import net.kyrptonaught.serverutils.AbstractConfigFile;

public class CPSLimitConfig extends AbstractConfigFile {

    public double smoothing = 0.7f;

    public double CPSLimit = 14;
}
