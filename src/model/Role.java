package model;

public class Role {
    private int RoleID;
    private String RoleName;

    public Role(){ }
    public Role(int roleID, String roleName){
        this.RoleID = roleID;
        this.RoleName = roleName;
    }
    public int getRoleID() {return RoleID;}
    public void setRoleID(int roleID) {this.RoleID = roleID;}
    public String getRoleName() {return RoleName;}
    public void setRoleName(String roleName) {this.RoleName = roleName;}

}
