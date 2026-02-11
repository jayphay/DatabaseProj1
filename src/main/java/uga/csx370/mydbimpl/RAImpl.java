package uga.csx370.mydbimpl;

import java.util.List;
import java.util.HashSet;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'select'");
    }

    @Override
    public Relation project(Relation rel, List<String> attrs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'project'");
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
        Predicate pred = (row) -> {
            // check the common attributes; if they're the same, return true
            for (String attr : commonAttrs) {
                if (!row.get(rel1.getAttrIndex(attr)).equals(row.get(rel2.getAttrIndex(i) + rel1.size())))
                    return false;
            }
            return true;
        };

        return join(rel1, rel2, pred);

    }

    @Override
    public Relation join(Relation rel1, Relation rel2, Predicate p) {
        // TODO Auto-generated method stub
        // this is same as cartesianProduct but without the Exception throwing when columns are same
        throw new UnsupportedOperationException("Unimplemented method 'join'");
    }

}