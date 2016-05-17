package br.com.fiap.pizza.dummy;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.core.Path;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.utilities.Validation;
import com.firebase.client.utilities.encoding.JsonHelpers;
import java.io.IOException;
import java.util.Iterator;

public  class MyDataSnapshot {

    private  IndexedNode node = null;
    private  Firebase query = null;



    public MyDataSnapshot(Firebase ref, IndexedNode node) {
        this.node = node;
        this.query = ref;
    }
    public MyDataSnapshot(){

    }

    public MyDataSnapshot child(String path) {
        Firebase childRef = this.query.child(path);
        Node childNode = this.node.getNode().getChild(new Path(path));
        return new MyDataSnapshot(childRef, IndexedNode.from(childNode));
    }

    public boolean hasChild(String path) {
        if(this.query.getParent() == null) {
            Validation.validateRootPathString(path);
        } else {
            Validation.validatePathString(path);
        }

        return !this.node.getNode().getChild(new Path(path)).isEmpty();
    }

    public boolean hasChildren() {
        return this.node.getNode().getChildCount() > 0;
    }

    public boolean exists() {
        return !this.node.getNode().isEmpty();
    }

    public Object getValue() {
        return this.node.getNode().getValue();
    }

    public Object getValue(boolean useExportFormat) {
        return this.node.getNode().getValue(useExportFormat);
    }

    public <T> T getValue(Class<T> valueType) {
        Object value = this.node.getNode().getValue();

        try {
            String e = JsonHelpers.getMapper().writeValueAsString(value);
            return JsonHelpers.getMapper().readValue(e, valueType);
        } catch (IOException var4) {
            throw new FirebaseException("Failed to bounce to type", var4);
        }
    }

    public <T> T getValue(GenericTypeIndicator<T> t) {
        Object value = this.node.getNode().getValue();

        try {
            String e = JsonHelpers.getMapper().writeValueAsString(value);
            return JsonHelpers.getMapper().readValue(e, t);
        } catch (IOException var4) {
            throw new FirebaseException("Failed to bounce to type", var4);
        }
    }

    public long getChildrenCount() {
        return (long)this.node.getNode().getChildCount();
    }

    public Firebase getRef() {
        return this.query;
    }

    public String getKey() {
        return this.query.getKey();
    }

    public Iterable<MyDataSnapshot> getChildren() {
        final Iterator iter = this.node.iterator();
        return new Iterable() {
            public Iterator<MyDataSnapshot> iterator() {
                return new Iterator() {
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    public MyDataSnapshot next() {
                        NamedNode namedNode = (NamedNode)iter.next();
                        return new MyDataSnapshot(MyDataSnapshot.this.query.child(namedNode.getName().asString()), IndexedNode.from(namedNode.getNode()));
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("remove called on immutable collection");
                    }
                };
            }
        };
    }

    public Object getPriority() {
        Object priority = this.node.getNode().getPriority().getValue();
        return priority instanceof Long?Double.valueOf((double)((Long)priority).longValue()):priority;
    }

    public String toString() {
        return "MyDataSnapshot { key = " + this.query.getKey() + ", value = " + this.node.getNode().getValue(true) + " }";
    }
}
