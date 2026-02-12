package uga.csx370.mydbimpl;

import java.util.List;
import java.util.HashSet;
import java.sql.Types;
import java.util.ArrayList;

import uga.csx370.mydb.Predicate;
import uga.csx370.mydb.RA;
import uga.csx370.mydb.Relation;
import uga.csx370.mydb.Cell;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;


public class RAImpl implements RA {

    @Override
    public Relation select(Relation rel, Predicate p) {
        //Creates A relation that will store rows that fulfill the predicate
        Relation rel1 = new RelationBuilder().attributeNames(rel.getAttrs()).attributeTypes(rel.getTypes()).build();

        //Checks if rows match predicate and puts them in relation
        for (int i = 0; i < rel.getSize(); i++) {
            if (p.check(rel.getRow(i))) {
                rel1.insert(rel.getRow(i));
            }
        }

        return rel1;

    }

    @Override

    public Relation project(Relation rel, List<String> attrs) {
        List<Type> attrTypes = rel.getTypes(); //gets all the attributes in a original Relation
        List<String> attrNames = rel.getAttrs(); //gets all the types in a original Relation
        List<Type> newAttrTypes = null; //creates a list to store the types associated with given attributes
        List<Integer> reqCol = new ArrayList<>(); // creates list to store where each required type is

        // stores where the required columns are
        for (String col : attrs) {
            int index = rel.getAttrIndex(col);
            reqCol.add(index);
        }

        // throws exception if the given attributes are not present
        if (reqCol.isEmpty()) {
            throw new IllegalArgumentException("Attributes not in list");
        }

        // stores the associated types
        for (int i = 0; i < attrTypes.size(); i++) {
            for  (int j = 0; j < attrs.size(); j++) {
                if (attrNames.get(i).equals(attrs.get(j))) {
                    if (newAttrTypes == null) {
                        newAttrTypes = new ArrayList<>();
                        newAttrTypes.add(attrTypes.get(i));
                    } else {
                        newAttrTypes.add(attrTypes.get(i));
                    }
                }
            }
        }

        // creates a new relation that will contain projected attributes
        Relation rel1 = new RelationBuilder().attributeNames(attrs).attributeTypes(newAttrTypes).build();

        // stores all required columns in new relation
        for (int i = 0; i < rel.getSize(); i++) {
            List<Cell> originalRow = rel.getRow(i);
            List<Cell> newRow = new ArrayList<>();
            for (int index : reqCol) {
                newRow.add(originalRow.get(index));
            }
            rel1.insert(newRow);
        }

        return rel1;

    }


    @Override
    public Relation union(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'union'");
    }

    @Override
    public Relation intersect(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersect'");
    }

    @Override
    public Relation diff(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'diff'");
    }

    @Override
    public Relation rename(Relation rel, List<String> origAttr, List<String> renamedAttr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rename'");
    }

    @Override
    public Relation cartesianProduct(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        // create new relation: in every row, put rel1 row and then rel2 row
        // m x n time; use nested loops, rel1 is outer, rel2 is inner
        // if rel1 and rel2 have common attrs, throw IllegalArgumentException
        List<String> attrs = new ArrayList<>();
        for (String attr : rel1.getAttrs())
            attrs.add(attr);
        for (String attr : rel2.getAttrs())
            attrs.add(attr);
        List<Type> types = new ArrayList<>();
        for (Type type : rel1.getTypes())
            types.add(type);
        for (Type type : rel2.getTypes())
            types.add(type);

        if (new HashSet<>(attrs).size() != rel1.getAttrs().size() + rel2.getAttrs().size()) {
            throw new IllegalArgumentException("The relations have common attributes");
        }
        Relation productRelation = new RelationBuilder()
        .attributeNames(attrs)
        .attributeTypes(types)
        .build();
        for (int i = 0; i < rel1.getSize(); i++) {
            List<Cell> rowToInsert = new ArrayList<>(); // reset the rel2 row info each time
            for (Cell item : rel1.getRow(i)) {
                rowToInsert.add(item);
            }

            for (int j = 0; j < rel2.getSize(); j++) {
                List<Cell> addRel2Row = new ArrayList<>(rowToInsert);
                for (Cell item : rel2.getRow(j)) {
                    addRel2Row.add(item);
                }

                productRelation.insert(addRel2Row);
            }
        }

        return productRelation;
    }

    @Override
    public Relation join(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        // look to see if any column names are the same: if none, return cartesianProduct
        // special case of theta join: join when columns are the same name and match relations
        // that have the same values in those columns
        List<String> commonAttrs = new ArrayList<>();
        for (String attr : rel1.getAttrs()) {
            if (rel2.getAttrs().contains(attr))
                commonAttrs.add(attr);
        }
        if (commonAttrs.size() == 0) {
            return cartesianProduct(rel1, rel2);
        }
        // return a Relation where each row has the info of rel1 and rel2 at that row
        // where the value in the shared column are equal

        // go through commonAttrs of both relations: if the value is equal, add rel1's cells to 
        // the row to insert and then add rel2's cells except for the commonAttr cells

        // build the attribute names/types: do all rel1 first and then rel2's that aren't in list already
        List<String> newAttrs = new ArrayList<>();
        List<Type> newTypes = new ArrayList<>();
        List<Integer> rel2CommonAttrIndices = new ArrayList<>();

        for (int i = 0; i < rel1.getAttrs().size(); i++) {
            newAttrs.add(rel1.getAttrs().get(i));
            newTypes.add(rel1.getTypes().get(i));
        }
        for (int i = 0; i < rel2.getAttrs().size(); i++) {
            if (!commonAttrs.contains(rel2.getAttrs().get(i))) {
                newAttrs.add(rel2.getAttrs().get(i));
                newTypes.add(rel2.getTypes().get(i));
            } else {
                // need to get array of indices that make up the commonAttr columns for rel2
                rel2CommonAttrIndices.add(i);
            }
        }

        Relation joinRelation = new RelationBuilder()
        .attributeNames(newAttrs)
        .attributeTypes(newTypes)
        .build();

        // go through each row of rel1 and rel2 and check the common attributes: 
        // if they're equal, add the row to joinRelation
        for (int i = 0; i < rel1.getSize(); i++) {
            List<Cell> rowToInsert = new ArrayList<>(); 
            for (Cell item : rel1.getRow(i)) {
                rowToInsert.add(item);
            }

            for (int j = 0; j < rel2.getSize(); j++) {
                List<Cell> addRel2Row = new ArrayList<>(rowToInsert);
                for (int k = 0; k < rel2.getRow(j).size(); k++) {
                    // don't add the cell if it's a commonAttr
                    if (!rel2CommonAttrIndices.contains(k))
                        addRel2Row.add(rel2.getRow(j).get(k));
                }

                // check the created row to add to make sure the commonAttrs are equal
                boolean addRelation = true;
                for (String attr : commonAttrs) {
                    if (!rowToInsert.get(rel1.getAttrIndex(attr)).equals(rel2.getRow(j).get(rel2.getAttrIndex(attr))))
                        addRelation = false;
                }
                if (addRelation) joinRelation.insert(addRel2Row);
            }
        }
        return joinRelation;

    }

    @Override
    public Relation join(Relation rel1, Relation rel2, Predicate p) {
        // TODO Auto-generated method stub
        // this is same as cartesianProduct but without the Exception throwing when columns are same
        throw new UnsupportedOperationException("Unimplemented method 'join'");
    }

}