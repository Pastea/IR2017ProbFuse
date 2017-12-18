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
    public int addModel()
    {
        handler.add(new LinkedList<>());
        return handler.size()-1;
    }

    public int addQuery(int model)
    {
        handler.get(model).add(new LinkedList<>());
        return handler.get(model).size()-1;
    }

    public int addSegment(int model, int query)
    {
        handler.get(model).get(query).add(new LinkedList<>());
        return handler.get(model).get(query).size()-1;
    }

    public int addLine(int model, int query, int segment, Utils.ResultLine line)
    {
        handler.get(model).get(query).get(segment).add(line);
        return handler.get(model).get(query).get(segment).size()-1;
    }

    public List<List<List<Utils.ResultLine>>> getModel(int model)
    {
        return handler.get(model);
    }

    public List<List<Utils.ResultLine>> getQuery(int model, int query)
    {
        return handler.get(model).get(query);
    }

    public List<Utils.ResultLine> getSegment(int model, int query, int segment)
    {
        return handler.get(model).get(query).get(segment);
    }

    public Utils.ResultLine getLine(int model, int query, int segment, int line)
    {
        return handler.get(model).get(query).get(segment).get(line);
    }

    public int getModelSize(int model)
    {

        return handler.get(model).size();
    }

    public int getQuerySize(int model, int query)
    {

        return handler.get(model).get(query).size();
    }

    public int getSegmentSize(int model, int query, int segment)
    {
        return handler.get(model).get(query).get(segment).size();
    }

    public List<List<List<Utils.ResultLine>>> removeModel(int model)
    {

        return handler.remove(model);
    }

    public List<List<Utils.ResultLine>> removeQuery(int model, int query)
    {

        return handler.get(model).remove(query);
    }

    public List<Utils.ResultLine> removeSegment(int model, int query, int segment)
    {
        return handler.get(model).get(query).remove(segment);
    }

    public Utils.ResultLine removeLine(int model, int query, int segment, int line)
    {
        return handler.get(model).get(query).get(segment).remove(line);
    }
}
