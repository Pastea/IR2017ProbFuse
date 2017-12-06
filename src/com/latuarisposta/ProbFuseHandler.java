package com.latuarisposta;

import java.util.LinkedList;
import java.util.List;

public class ProbFuseHandler
{
    List<List<List<List<Utils.ResultLine>>>> handler = null;

    public ProbFuseHandler()
    {
        handler = new LinkedList<>();
    }

    public int addSystem()
    {
        handler.add(new LinkedList<>());
        return handler.size()-1;
    }

    public int addQuery(int system)
    {
        handler.get(system).add(new LinkedList<>());
        return handler.get(system).size()-1;
    }

    public int addSegment(int system, int query)
    {
        handler.get(system).get(query).add(new LinkedList<>());
        return handler.get(system).get(query).size()-1;
    }

    public int addLine(int system, int query, int segment, Utils.ResultLine line)
    {
        handler.get(system).get(query).get(segment).add(line);
        return handler.get(system).get(query).get(segment).size()-1;
    }

    public List<List<List<Utils.ResultLine>>> getSystem(int system)
    {
        return handler.get(system);
    }

    public List<List<Utils.ResultLine>> getQuery(int system, int query)
    {
        return handler.get(system).get(query);
    }

    public List<Utils.ResultLine> getSegment(int system, int query, int segment)
    {
        return handler.get(system).get(query).get(segment);
    }

    public Utils.ResultLine getLine(int system, int query, int segment, int line)
    {
        return handler.get(system).get(query).get(segment).get(line);
    }

    public int getSystemSize(int system)
    {
        return handler.get(system).size();
    }

    public int getQuerySize(int system, int query)
    {
        return handler.get(system).get(query).size();
    }

    public int getSegmentSize(int system, int query, int segment)
    {
        return handler.get(system).get(query).get(segment).size();
    }

    public List<List<List<Utils.ResultLine>>> removeSystem(int system)
    {
        return handler.remove(system);
    }

    public List<List<Utils.ResultLine>> removeQuery(int system, int query)
    {
        return handler.get(system).remove(query);
    }

    public List<Utils.ResultLine> removeSegment(int system, int query, int segment)
    {
        return handler.get(system).get(query).remove(segment);
    }

    public Utils.ResultLine removeLine(int system, int query, int segment, int line)
    {
        return handler.get(system).get(query).get(segment).remove(line);
    }
}
