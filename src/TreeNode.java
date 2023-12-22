import java.util.LinkedList;
import java.util.Queue;

class TreeNode<T>{
    T key;
    long val;
    TreeNode<T> left;
    TreeNode<T> right;

    public TreeNode(T key, long val, TreeNode<T> left, TreeNode<T> right){
        this.key = key;
        this.val = val;
        this.left = left;
        this.right = right;
    }
    public TreeNode<T> min(TreeNode<T> b){
        return (this.val <= b.val)? this : b;
    }
    public TreeNode<T> max(TreeNode<T> b){
        return (this.val > b.val)? this : b;
    }
    public TreeNode<T> mergeWith(TreeNode<T> t, T newKey){
        return new TreeNode<>(
            newKey,
            this.val + t.val,
            this.min(t),
            this.max(t)
        );
    }
    @Override
    public String toString(){
        if(key == null){
            return "{ - : "+this.val+"}";
        }
        return "{" + this.key.toString() + ": " + this.val + "}";
    }
    public void bfs(){
        Queue<TreeNode<T>> q = new LinkedList<>();
        q.add(this);
        while(!q.isEmpty()){
        TreeNode<T> curr = q.poll();
        if(curr.left != null){q.add(curr.left);}
        if(curr.right != null){q.add(curr.right);}
        }
    }

}